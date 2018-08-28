package Expenses.Model

import java.util.Date

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.{FunSpec, Matchers}
import squants.market.Money

class FoodExpenseTest extends FunSpec with Matchers {
  val dateInThePast = new Date(new Date().getTime - 1)
  val invalidCost = Money(50, "EUR")
  val validCost = Money(49.99, "EUR")

  describe("createFood") {
    it("should return invalid when cost is more than or equal to 50") {
      Expense.createFood(invalidCost, dateInThePast) should matchPattern {
        case Invalid(NonEmptyList("cost is greater than or equal to 50", _)) =>
      }
    }
    it("should return a food expense") {
      Expense.createFood(validCost, dateInThePast) should matchPattern {
        case Valid(FoodExpense(`validCost`, `dateInThePast`)) =>
      }
    }
  }
}
