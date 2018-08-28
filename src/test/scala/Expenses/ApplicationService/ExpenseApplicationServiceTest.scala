package Expenses.ApplicationService

import java.util.{Date, UUID}

import Expenses.ApplicationServices.ExpenseApplicationService
import Expenses.Model._
import Expenses.TestUtils.AcceptanceTestUtils.{Test, TestState}
import Expenses.TestUtils.{InMemoryClaimRepository, InMemoryEmployeeRepository, InMemoryExpenseSheetRepository}
import cats.data.NonEmptyList
import org.scalatest.{FunSpec, Matchers}
import squants.market.Money

class ExpenseApplicationServiceTest extends FunSpec with Matchers {
  implicit val er: InMemoryEmployeeRepository = new InMemoryEmployeeRepository()
  implicit val esr: InMemoryExpenseSheetRepository = new InMemoryExpenseSheetRepository()
  implicit val cr: InMemoryClaimRepository = new InMemoryClaimRepository()

  describe("openFor") {
    it("should save a new OpenExpenseSheet for the given employee") {
      val employee = Employee.create("A", "V").toOption.get
      val newState = ExpenseApplicationService.openFor[Test](employee.id)
        .runS(TestState(List(employee), List(), List())).value

      newState.expenseSheets should matchPattern {
        case List(OpenExpenseSheet(_, `employee`, List())) =>
      }
    }
    it("should not create an expense sheet when employee does not exist") {
      val (newState, result) = ExpenseApplicationService.openFor[Test](UUID.randomUUID())
        .run(emptyState()).value

      newState should equal(emptyState())
      result should matchPattern {
        case Left(NonEmptyList(s, _)) if s.toString.startsWith("Unable to find employee ") =>
      }
    }
  }
  describe("addExpenseTo") {
    it("should add an expense to an open expense sheet") {
      val employee = Employee.create("A", "V").toOption.get
      val expense = Expense.createTravel(Money(1, "EUR"), new Date(), "Florence", "Barcelona").toOption.get
      val expenseSheet = ExpenseSheet.createOpen(employee, List()).toOption.get
      val newState = ExpenseApplicationService.addExpenseTo[Test](expense, expenseSheet.id)
        .runS(TestState(List(employee), List(expenseSheet), List())).value

      newState.expenseSheets should be(List(OpenExpenseSheet(expenseSheet.id, employee, List(expense))))
    }
    it("should return error when expense sheet is missing") {
      val expense = Expense.createTravel(Money(1, "EUR"), new Date(), "Florence", "Barcelona").toOption.get
      val id = UUID.randomUUID()
      val (newState, result) = ExpenseApplicationService.addExpenseTo[Test](expense, id)
        .run(emptyState()).value

      newState should equal(emptyState())
      result should be(Left(NonEmptyList.of(s"Unable to find expense sheet $id")))
    }
    it("should not add an expense to a claimed expense sheet") {
      val employee = Employee.create("A", "V").toOption.get
      val expense = Expense.createTravel(Money(1, "EUR"), new Date(), "Florence", "Barcelona").toOption.get
      val expenseSheet = ExpenseSheet.createClaimed(employee, List(expense)).toOption.get
      val (newState, result) = ExpenseApplicationService.addExpenseTo[Test](expense, expenseSheet.id)
        .run(TestState(List(employee), List(expenseSheet), List())).value

      result should matchPattern {
        case Left(NonEmptyList(s, _)) if s.toString.endsWith(" is not an open expense sheet") =>
      }
      newState.expenseSheets should be(List(expenseSheet))
    }
  }
  describe("claim") {
    it("should save claimed expense sheet and pending claim") {
      val employee = Employee.create("A", "V").toOption.get
      val expenses = List(TravelExpense(Money(1, "EUR"), new Date(), "Florence", "Barcelona"))
      val expenseSheet = ExpenseSheet.createOpen(employee, expenses).toOption.get
      val newState = ExpenseApplicationService.claim[Test](expenseSheet.id)
        .runS(TestState(List(employee), List(expenseSheet), List())).value

      newState.claims should matchPattern {
        case List(PendingClaim(_, `employee`, _)) =>
      }
      newState.expenseSheets should be (List(ClaimedExpenseSheet(expenseSheet.id, employee, expenses)))
    }
  }

  def emptyState() = TestState(List(), List(), List())
}
