package Infrastructure

import java.util.{Date, UUID}

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model._
import Expenses.Repositories.ExpenseSheetRepository
import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers}
import squants.market.Money

import scala.collection.mutable.ListBuffer

abstract class ExpenseSheetRepositoryContractTest[F[_]](implicit M:Monad[F])
  extends FunSpec with Matchers with BeforeAndAfterEach {
  val toBeDeletedExpenseSheetIds: ListBuffer[ExpenseSheetId] = ListBuffer.empty[ExpenseSheetId]

  describe ("get") {
    it("should retrieve existing open expense sheet") {
      val id = UUID.randomUUID()
      val employee = Employee(UUID.randomUUID(), "Andrea", "Vallotti")
      val sut = createRepositoryWith(List(OpenExpenseSheet(id, employee, List())), List(employee))

      run(sut.get(id)) should matchPattern {
        case Right(OpenExpenseSheet(`id`, `employee`, List())) =>
      }
    }
    it("should retrieve existing open expense sheet w/ expenses") {
      val id = UUID.randomUUID()
      val employee = Employee(UUID.randomUUID(), "Andrea", "Vallotti")
      val expenses = List(
        TravelExpense(Money(1, "EUR"), new Date(), "Florence", "Barcelona"),
        FoodExpense(Money(2, "EUR"), new Date()),
        AccommodationExpense(Money(2, "EUR"), new Date(), "Artemide"),
        OtherExpense(Money(2, "EUR"), new Date(), "A very long description about how I spend money"))
      val sut = createRepositoryWith(List(OpenExpenseSheet(id, employee, expenses)), List(employee))

      run(sut.get(id)) should matchPattern {
        case Right(OpenExpenseSheet(`id`, `employee`, `expenses`)) =>
      }
    }
    it("should retrieve existing claimed expense sheet") {
      val id = UUID.randomUUID()
      val employee = Employee(UUID.randomUUID(), "Andrea", "Vallotti")
      val expenses = List(
        TravelExpense(Money(1, "EUR"), new Date(), "Florence", "Barcelona"))
      val sut = createRepositoryWith(List(ClaimedExpenseSheet(id, employee, expenses)), List(employee))

      run(sut.get(id)) should matchPattern {
        case Right(ClaimedExpenseSheet(`id`, `employee`, `expenses`)) =>
      }
    }
    it("should return left when get a missing expense sheet") {
      val id = UUID.randomUUID()
      val sut = createRepositoryWith(List(), List())

      run(sut.get(id)) should be(Left(NonEmptyList.of(s"Unable to find expense sheet $id")))
    }
  }
  describe("save") {
    it("should save when employee exists") {
      val employee = Employee(UUID.randomUUID(), "A", "V")
      val sut = createRepositoryWith(List(), List(employee))
      val expenseSheet = OpenExpenseSheet(UUID.randomUUID(), employee, List())

      toBeDeletedExpenseSheetIds += expenseSheet.id

      run(for {
        _ <- sut.save(expenseSheet)
        check <- existExpenseSheet(expenseSheet.id)
      } yield check) should be(true)
    }
    it("should not save when employee does not exist") {
      val employee = Employee(UUID.randomUUID(), "A", "V")
      val sut = createRepositoryWith(List(), List())
      val expenseSheet = OpenExpenseSheet(UUID.randomUUID(), employee, List())

      val (result, check) = run(for {
        result <- sut.save(expenseSheet)
        check <- existExpenseSheet(expenseSheet.id)
      } yield (result, check))

      result should be(Left(NonEmptyList(s"Unable to find employee ${employee.id}", List())))
      check should be(false)
    }
    it("should update an existing expense sheet") {
      val employee = Employee(UUID.randomUUID(), "A", "V")
      val expenseSheet = OpenExpenseSheet(UUID.randomUUID(), employee, List())
      val sut = createRepositoryWith(List(expenseSheet), List(employee))
      val expense = FoodExpense(Money(1, "EUR"), new Date())
      val updatedExpenseSheet = ClaimedExpenseSheet(expenseSheet.id, employee, List(expense))

      run(for {
        _ <- sut.save(updatedExpenseSheet)
        gotExpenseSheet <- sut.get(expenseSheet.id)
      } yield gotExpenseSheet) should matchPattern {
        case Right(`updatedExpenseSheet`) =>
      }
    }
  }

  override def afterEach(): Unit = {
    cleanUp(toBeDeletedExpenseSheetIds.toList)
  }

  def createRepositoryWith(expenseSheets: List[ExpenseSheet], employees: List[Employee] = List()): ExpenseSheetRepository[F]

  def run[A](toBeExecuted: F[A]) : A

  def cleanUp(expenseSheetIds: List[ExpenseSheetId]): Unit

  def existExpenseSheet(id: ExpenseSheetId) : F[Boolean]
}
