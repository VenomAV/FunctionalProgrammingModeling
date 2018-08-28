package Expenses.Utils

import java.util.{Calendar, Date}

import cats.ApplicativeError
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}
import cats.implicits.catsSyntaxValidatedId

object ErrorManagement {
  type Validated[A] = ValidatedNel[String, A]

  def notEmptyString(errorMessage: String)(value: String): Validated[String] =
    if (value == null || "".equals(value))
      errorMessage.invalidNel
    else
      value.validNel

  def dateInThePastOrToday(errorMessage: String)(date: Date): Validated[Date] = {
    if (date == null || date.after(Calendar.getInstance.getTime))
      errorMessage.invalidNel
    else
      date.validNel
  }

  def notNull[T](errorMessage: String)(value: T) : Validated[T] = {
    if (value == null)
      errorMessage.invalidNel
    else
      value.validNel
  }

  def nonEmptyList[T](errorMessage: String)(list: List[T]): Validated[List[T]] =
    list match {
      case _ :: _ => list.validNel
      case _ => errorMessage.invalidNel
    }

  object implicits {
    implicit class ValidationResultToApplicativeError[A](val vr: Validated[A]) extends AnyVal {
      def orRaiseError[F[_]](implicit AE:ApplicativeError[F, Throwable]): F[A] = vr match {
        case Valid(x) => AE.pure(x)
        case Invalid(nel: NonEmptyList[String]) => AE.raiseError(new java.lang.Error(nel.toList.mkString(", ")))
      }
    }
  }
}
