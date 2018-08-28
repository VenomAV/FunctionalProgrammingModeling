package Expenses.Services

import java.util.{Date, UUID}

import Expenses.Model.{AccommodationExpense, Employee, FoodExpense, OpenExpenseSheet}
import cats.data.Validated.Valid
import org.scalatest.{FunSpec, Matchers}
import squants.market.Money

class ExpenseServiceTest extends FunSpec with Matchers {
  val employee = Employee(UUID.randomUUID(), "Andrea", "Vallotti")
  val openExpenseSheet = OpenExpenseSheet(UUID.randomUUID(), employee, List())
  val expense = FoodExpense(Money(1, "EUR"), new Date())
  val openExpenseSheetWithExpense = OpenExpenseSheet(UUID.randomUUID(), employee, List(expense))

  describe("openFor") {
    it("should return an open expense sheet") {
      ExpenseService.openFor(employee) should matchPattern {
        case Valid(OpenExpenseSheet(_, `employee`, List())) =>
      }
    }
  }
  describe("addExpenseTo") {
    it("should return an open expense sheet with the given expense") {
      val expenseSheetId = openExpenseSheet.id
      ExpenseService.addExpenseTo(expense, openExpenseSheet) should matchPattern {
        case Valid(OpenExpenseSheet(`expenseSheetId`, `employee`, List(`expense`))) =>
      }
    }
    it("should return an open expense sheet with the given expense and the existing ones") {
      val expenseSheetId = openExpenseSheetWithExpense.id
      val newExpense = AccommodationExpense(Money(2, "EUR"), new Date(), "Hotel degli Arcimboldi")
      ExpenseService.addExpenseTo(newExpense, openExpenseSheetWithExpense) should matchPattern {
        case Valid(OpenExpenseSheet(`expenseSheetId`, `employee`, List(`expense`, `newExpense`))) =>
      }
    }
  }
}
