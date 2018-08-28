package Expenses.Services

import java.util.Date

import Expenses.Model._
import Expenses.TestUtils.AcceptanceTestUtils.OrError
import cats.data.NonEmptyList
import cats.implicits._
import org.scalatest.{FunSpec, Matchers}
import squants.market.Money

class ExpenseServiceTest extends FunSpec with Matchers {
  private val employee = Employee.create("A", "V").toOption.get
  private val openExpenseSheet = ExpenseSheet.createOpen(employee, List()).toOption.get
  private val expense = Expense.createFood(Money(1, "EUR"), new Date()).toOption.get
  private val openExpenseSheetWithExpense = ExpenseSheet.createOpen(employee, List(expense)).toOption.get

  describe("openFor") {
    it("should return an open expense sheet") {
      ExpenseService.openFor[OrError](employee) should matchPattern {
        case Right(OpenExpenseSheet(_, `employee`, List())) =>
      }
    }
  }
  describe("addExpenseTo") {
    it("should return an open expense sheet with the given expense") {
      ExpenseService.addExpenseTo[OrError](expense, openExpenseSheet) should
        be(Right(OpenExpenseSheet(openExpenseSheet.id, employee, List(expense))))
    }
    it("should return an open expense sheet with the given expense and the existing ones") {
      val newExpense = AccommodationExpense(Money(2, "EUR"), new Date(), "Hotel degli Arcimboldi")
      ExpenseService.addExpenseTo[OrError](newExpense, openExpenseSheetWithExpense) should
        be(Right(OpenExpenseSheet(openExpenseSheetWithExpense.id, employee, List(expense, newExpense))))
    }
  }
  describe("claim") {
    it("should claim an open expense sheet") {
      val id = openExpenseSheetWithExpense.id
      val expenses = List(expense)

      ExpenseService.claim[OrError](openExpenseSheetWithExpense) should
        matchPattern {
          case Right((ClaimedExpenseSheet(`id`, `employee`, `expenses`),
            PendingClaim(_, `employee`, NonEmptyList(`expense`, List())))) =>
        }
    }
    it("should NOT claim an empty open expense sheet") {
      ExpenseService.claim[OrError](openExpenseSheet) should matchPattern {
        case Left(ex : Throwable) if ex.getMessage.equals("Cannot claim empty expense sheet") =>
      }
    }
  }
}
