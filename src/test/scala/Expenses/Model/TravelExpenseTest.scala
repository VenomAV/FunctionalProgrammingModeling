package Expenses.Model

import java.util.Date

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.{FunSpec, Matchers}
import squants.market.Money

class TravelExpenseTest extends FunSpec with Matchers {
  val dateInThePast = new Date(new Date().getTime - 1)
  val dateInTheFuture = new Date(new Date().getTime + 1000)
  val cost = Money(1, "EUR")
  val from = "Florence"
  val to = "Barcelona"

  describe("createTravel") {
    it("should return invalid when date is in the future") {
      Expense.createTravel(cost, dateInTheFuture, from, to) should matchPattern {
        case Invalid(NonEmptyList("date cannot be in the future", _)) =>
      }
    }
    it("should return invalid when 'to' is empty") {
      Expense.createTravel(cost, dateInThePast, from, "") should matchPattern {
        case Invalid(NonEmptyList("to is null or empty", _)) =>
      }
    }
    it("should return invalid when 'to' is null") {
      Expense.createTravel(cost, dateInThePast, from, null) should matchPattern {
        case Invalid(NonEmptyList("to is null or empty", _)) =>
      }
    }
    it("should return invalid when 'from' is empty") {
      Expense.createTravel(cost, dateInThePast, "", to) should matchPattern {
        case Invalid(NonEmptyList("from is null or empty", _)) =>
      }
    }
    it("should return invalid when 'from' is null") {
      Expense.createTravel(cost, dateInThePast, null, to) should matchPattern {
        case Invalid(NonEmptyList("from is null or empty", _)) =>
      }
    }
    it("should return invalid when cost is zero") {
      Expense.createTravel(Money(0, "EUR"), dateInThePast, from, to) should matchPattern {
        case Invalid(NonEmptyList("cost is less or equal to zero", _)) =>
      }
    }
    it("should return invalid when cost is negative") {
      Expense.createTravel(Money(-1, "EUR"), dateInThePast, from, to) should matchPattern {
        case Invalid(NonEmptyList("cost is less or equal to zero", _)) =>
      }
    }
    it("should return a travel expense") {
      Expense.createTravel(cost, dateInThePast, from , to) should matchPattern {
        case Valid(TravelExpense(`cost`, `dateInThePast`, `from`, `to`)) =>
      }
    }
  }
}