package Expenses.Model

import java.util.Date

import Expenses.Utils.ErrorManagement
import Expenses.Utils.ErrorManagement.Validated
import squants.market.Money
import cats.implicits._

sealed trait Expense {
  def cost: Money

  def date: Date
}

case class TravelExpense private (cost: Money, date: Date, from: String, to: String) extends Expense

case class FoodExpense private (cost: Money, date: Date) extends Expense

case class AccommodationExpense private (cost: Money, date: Date, hotel: String) extends Expense

case class OtherExpense private (cost: Money, date: Date, description: String) extends Expense

object Expense {
  private val validateDate =
    ErrorManagement.dateInThePastOrToday("date cannot be in the future")(_)

  private def validateCost(cost: Money): Validated[Money] =
    if (cost.amount <= 0) "cost is less or equal to zero".invalidNel
    else cost.validNel

  private val validateTo = ErrorManagement.notEmptyString("to is null or empty")(_)
  private val validateFrom = ErrorManagement.notEmptyString("from is null or empty")(_)

  def createTravel(cost: Money, date: Date, from: String, to: String): Validated[TravelExpense] =
    (validateCost(cost), validateDate(date), validateFrom(from), validateTo(to))
      .mapN(TravelExpense)

  private def maxCostLimitValidation(cost: Money): Validated[Money] =
    if (cost.amount >= 50)
      "cost is greater than or equal to 50".invalidNel
    else
      cost.validNel

  def createFood(cost: Money, date: Date): Validated[FoodExpense] =
    (validateCost(cost), validateDate(date), maxCostLimitValidation(cost))
      .mapN((c, d, _) => FoodExpense(c, d))

  private val validateHotel = ErrorManagement.notEmptyString("hotel is null or empty")(_)

  def createAccommodation(cost: Money, date: Date, hotel: String): Validated[AccommodationExpense] =
    (validateCost(cost), validateDate(date), validateHotel(hotel))
      .mapN(AccommodationExpense)

  private def countWords(description: String): Int = {
    description.split(" ").map(_.trim).count(!_.isEmpty)
  }

  private def validateDescription(description: String): Validated[String] =
    if (countWords(description) < 10)
      "description contains less than 10 words".invalidNel
    else
      description.validNel

  def createOther(cost: Money, date: Date, description: String): Validated[OtherExpense] =
    (validateCost(cost), validateDate(date), validateDescription(description))
      .mapN(OtherExpense)
}
