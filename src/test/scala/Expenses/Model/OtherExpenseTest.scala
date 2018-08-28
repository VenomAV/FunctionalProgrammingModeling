package Expenses.Model

import java.util.Date

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.{FunSpec, Matchers}
import squants.market.Money

class OtherExpenseTest extends FunSpec with Matchers {
  val dateInThePast = new Date(new Date().getTime - 1)
  val cost = Money(1, "EUR")

  describe("createFood") {
    it("should return invalid when description contains less than 10 words") {
      Expense.createOther(cost, dateInThePast, "a b c d e f g h i") should matchPattern {
        case Invalid(NonEmptyList("description contains less than 10 words", _)) =>
      }
    }
    it("should return a other expense") {
      Expense.createOther(cost, dateInThePast, "a b c d e f g h i j") should matchPattern {
        case Valid(OtherExpense(`cost`, `dateInThePast`, "a b c d e f g h i j")) =>
      }
    }
  }
}