package Infrastructure

import java.util.Date

import Expenses.Model._
import io.circe.syntax._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, ObjectEncoder}
import squants.market.Money

object JsonCodecs {
  object implicits {
    implicit val dateEncodeJson: Encoder[Date] = Encoder.forProduct1("time") {
      d : Date => d.getTime
    }
    implicit val dateDecodeJson: Decoder[Date] = Decoder.forProduct1("time") {
      time: Long => new Date(time)
    }

    implicit val moneyEncodeJson: Encoder[Money] = Encoder.forProduct2("amount", "currency") {
      m : Money => (m.amount, m.currency.toString())
    }
    implicit val moneyDecodeJson: Decoder[Money] = Decoder.forProduct2("amount", "currency") {
      (amount: BigDecimal, currency: String) => Money(amount, currency)
    }

    implicit val travelExpenseEncodeJson: ObjectEncoder[TravelExpense] = deriveEncoder
    implicit val travelExpenseDecodeJson: Decoder[TravelExpense] = deriveDecoder

    implicit val foodExpenseEncodeJson: ObjectEncoder[FoodExpense] = deriveEncoder
    implicit val foodExpenseDecodeJson: Decoder[FoodExpense] = deriveDecoder

    implicit val accommodationExpenseEncodeJson: ObjectEncoder[AccommodationExpense] = deriveEncoder
    implicit val accommodationExpenseDecodeJson: Decoder[AccommodationExpense] = deriveDecoder

    implicit val otherExpenseEncodeJson: ObjectEncoder[OtherExpense] = deriveEncoder
    implicit val otherExpenseDecodeJson: Decoder[OtherExpense] = deriveDecoder

    implicit val expenseEncodeJson: Encoder[Expense] = Encoder.instance[Expense] {
      case te @ TravelExpense(_, _, _, _) => te.asJsonObject.add("type", "travel".asJson).asJson
      case fe @ FoodExpense(_, _) => fe.asJsonObject.add("type", "food".asJson).asJson
      case ae @ AccommodationExpense(_, _, _) => ae.asJsonObject.add("type", "accommodation".asJson).asJson
      case oe @ OtherExpense(_, _, _) => oe.asJsonObject.add("type", "other".asJson).asJson
      case _ => throw new UnsupportedOperationException
    }

    implicit val expenseDecodeJson : Decoder[Expense] = for {
      expenseType <- Decoder[String].prepare(_.downField("type"))
      expense <- expenseType match {
        case "travel" => Decoder[TravelExpense]
        case "food" => Decoder[FoodExpense]
        case "accommodation" => Decoder[AccommodationExpense]
        case "other" => Decoder[OtherExpense]
        case other => Decoder.failedWithMessage(s"invalid type: $other")
      }
    } yield expense

    implicit val expenseListEncodeJson: Encoder[List[Expense]] = Encoder.encodeList[Expense]

    implicit val expenseListDecodeJson: Decoder[List[Expense]] = Decoder.decodeList[Expense]
  }
}
