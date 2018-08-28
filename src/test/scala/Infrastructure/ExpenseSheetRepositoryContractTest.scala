package Infrastructure

import java.util.{Date, UUID}

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model._
import Expenses.Repositories.ExpenseSheetRepository
import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers}
import squants.market.Money

import scala.collection.mutable.ListBuffer

abstract class ExpenseSheetRepositoryContractTest[F[_]](implicit ME:MonadError[F, Throwable])
  extends FunSpec with Matchers with BeforeAndAfterEach {
  val toBeDeletedExpenseSheetIds: ListBuffer[ExpenseSheetId] = ListBuffer.empty[ExpenseSheetId]
  val employee: Employee = Employee.create("A", "V").toOption.get
  val travelExpense: Expense = Expense.createTravel(Money(1, "EUR"), new Date(), "Florence", "Barcelona").toOption.get
  val foodExpense: Expense = Expense.createFood(Money(2, "EUR"), new Date()).toOption.get

  describe ("get") {
    it("should retrieve existing open expense sheet") {
      val openExpenseSheet = ExpenseSheet.createOpen(employee, List()).toOption.get
      val sut = createRepositoryWith(List(openExpenseSheet), List(employee))

      run(sut.get(openExpenseSheet.id)) should be(Right(openExpenseSheet))
    }
    it("should retrieve existing open expense sheet w/ expenses") {
      val expenses = List(
        travelExpense, foodExpense,
        Expense.createAccommodation(Money(2, "EUR"), new Date(), "Artemide").toOption.get,
        Expense.createOther(Money(2, "EUR"), new Date(), "A very long description about what I spent money for").toOption.get)
      val openExpenseSheet = ExpenseSheet.createOpen(employee, expenses).toOption.get
      val sut = createRepositoryWith(List(openExpenseSheet), List(employee))

      run(sut.get(openExpenseSheet.id)) should be(Right(openExpenseSheet))
    }
    it("should retrieve existing claimed expense sheet") {
      val expenses = List(travelExpense)
      val claimedExpenseSheet = ExpenseSheet.createClaimed(employee, expenses).toOption.get
      val sut = createRepositoryWith(List(claimedExpenseSheet), List(employee))

      run(sut.get(claimedExpenseSheet.id)) should be(Right(claimedExpenseSheet))
    }
    it("should return left when get a missing expense sheet") {
      val id = UUID.randomUUID()
      val sut = createRepositoryWith(List(), List())

      run(sut.get(id)) should matchPattern{
        case Left(x : Throwable) if x.getMessage.equals(s"Unable to find expense sheet $id") =>
      }
    }
  }
  describe("save") {
    it("should save when employee exists") {
      val sut = createRepositoryWith(List(), List(employee))
      val expenseSheet = ExpenseSheet.createOpen(employee, List()).toOption.get

      toBeDeletedExpenseSheetIds += expenseSheet.id

      run(for {
        _ <- sut.save(expenseSheet)
        _ <- sut.get(expenseSheet.id)
      } yield ()) should be(Right(()))
    }
    it("should not save when employee does not exist") {
      val sut = createRepositoryWith(List(), List())
      val expenseSheet = ExpenseSheet.createOpen(employee, List()).toOption.get

      run(sut.save(expenseSheet)) should matchPattern {
        case Left(x : Throwable) if x.getMessage.equals(s"Unable to find employee ${employee.id}") =>
      }
    }
    it("should update an existing expense sheet") {
      val expenseSheet = ExpenseSheet.createOpen(employee, List()).toOption.get
      val sut = createRepositoryWith(List(expenseSheet), List(employee))
      val updatedExpenseSheet = ExpenseSheet.createClaimed(expenseSheet.id, employee, List(foodExpense)).toOption.get

      run(for {
        _ <- sut.save(updatedExpenseSheet)
        gotExpenseSheet <- sut.get(expenseSheet.id)
      } yield gotExpenseSheet) should be(Right(updatedExpenseSheet))
    }
  }

  override def afterEach(): Unit = {
    cleanUp(toBeDeletedExpenseSheetIds.toList)
  }

  def createRepositoryWith(expenseSheets: List[ExpenseSheet], employees: List[Employee] = List()): ExpenseSheetRepository[F]

  def run[A](toBeExecuted: F[A]) : Either[Throwable, A]

  def cleanUp(expenseSheetIds: List[ExpenseSheetId]): Unit
}
