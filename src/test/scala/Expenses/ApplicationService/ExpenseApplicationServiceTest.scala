package Expenses.ApplicationService

import java.util.{Date, UUID}

import Expenses.ApplicationServices.ExpenseApplicationServiceME
import Expenses.Model._
import Expenses.Repositories.{ClaimRepositoryME, EmployeeRepositoryME, ExpenseSheetRepositoryME}
import Expenses.TestUtils.AcceptanceTestUtils.{TestME, TestState}
import Expenses.TestUtils._
import cats.implicits._
import org.scalatest.{FunSpec, Matchers}
import squants.market.Money

class ExpenseApplicationServiceTest extends FunSpec with Matchers {
  implicit val er: EmployeeRepositoryME[TestME] = new InMemoryEmployeeMERepository()
  implicit val esr: ExpenseSheetRepositoryME[TestME] = new InMemoryExpenseSheetRepositoryME()
  implicit val cr: ClaimRepositoryME[TestME] = new InMemoryClaimRepositoryME()
  val employee: Employee = Employee.create("A", "V").toOption.get
  val expense: Expense = Expense.createTravel(Money(1, "EUR"), new Date(), "Florence", "Barcelona").toOption.get

  describe("openFor") {
    it("should save a new OpenExpenseSheet for the given employee") {
      val newState = ExpenseApplicationServiceME.openFor[TestME](employee.id)
        .runS(TestState(List(employee), List(), List())).getOrElse(emptyState())

      newState.expenseSheets should matchPattern {
        case List(OpenExpenseSheet(_, `employee`, List())) =>
      }
    }
    it("should not create an expense sheet when employee does not exist") {
      val result = ExpenseApplicationServiceME.openFor[TestME](UUID.randomUUID())
        .runA(emptyState())

      result should matchPattern {
        case Left(ex : Throwable) if ex.getMessage.startsWith("Unable to find employee ") =>
      }
    }
  }
  describe("addExpenseTo") {
    it("should add an expense to an open expense sheet") {
      val expenseSheet = ExpenseSheet.createOpen(employee, List()).toOption.get
      val newState = ExpenseApplicationServiceME.addExpenseTo[TestME](expense, expenseSheet.id)
        .runS(TestState(List(employee), List(expenseSheet), List()))
        .getOrElse(emptyState())

      newState.expenseSheets should be(List(OpenExpenseSheet(expenseSheet.id, employee, List(expense))))
    }
    it("should return error when expense sheet is missing") {
      val id = UUID.randomUUID()
      val result = ExpenseApplicationServiceME.addExpenseTo[TestME](expense, id)
        .runA(emptyState())

      result should matchPattern {
        case Left(ex : Throwable) if ex.getMessage.startsWith(s"Unable to find expense sheet $id") =>
      }
    }
    it("should not add an expense to a claimed expense sheet") {
      val expenseSheet = ExpenseSheet.createClaimed(employee, List(expense)).toOption.get
      val result = ExpenseApplicationServiceME.addExpenseTo[TestME](expense, expenseSheet.id)
        .runA(TestState(List(employee), List(expenseSheet), List()))

      result should matchPattern {
        case Left(ex : Throwable) if ex.getMessage.startsWith(s"${expenseSheet.id} is not an open expense sheet") =>
      }
    }
  }
  describe("claim") {
    it("should save claimed expense sheet and pending claim") {
      val expenseSheet = ExpenseSheet.createOpen(employee, List(expense)).toOption.get
      val newState = ExpenseApplicationServiceME.claim[TestME](expenseSheet.id)
        .runS(TestState(List(employee), List(expenseSheet), List()))
        .getOrElse(emptyState())

      newState.claims should matchPattern {
        case List(PendingClaim(_, `employee`, _)) =>
      }
      newState.expenseSheets should be (List(ClaimedExpenseSheet(expenseSheet.id, employee, List(expense))))
    }
  }

  def emptyState() = TestState(List(), List(), List())
}
