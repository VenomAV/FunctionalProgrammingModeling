package Expenses.Model

import java.util.Date

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.{FunSpec, Matchers}
import squants.market.Money

class AccommodationExpenseTest extends FunSpec with Matchers {
  val dateInThePast = new Date(new Date().getTime - 1)
  val cost = Money(1, "EUR")

  describe("createAccommodation") {
    it("should return invalid when hotel is empty") {
      Expense.createAccommodation(cost, dateInThePast, "") should matchPattern {
        case Invalid(NonEmptyList("hotel is null or empty", _)) =>
      }
    }
    it("should return invalid when hotel is null") {
      Expense.createAccommodation(cost, dateInThePast, null) should matchPattern {
        case Invalid(NonEmptyList("hotel is null or empty", _)) =>
      }
    }
    it("should return an accommodation expense") {
      Expense.createAccommodation(cost, dateInThePast, "Hotel degli Arcimboldi") should matchPattern {
        case Valid(AccommodationExpense(`cost`, `dateInThePast`, "Hotel degli Arcimboldi")) =>
      }
    }
  }
}


