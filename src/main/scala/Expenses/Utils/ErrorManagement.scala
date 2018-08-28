package Expenses.Utils

import java.util.{Calendar, Date}

import cats.data.{NonEmptyList, ValidatedNel}
import cats.implicits._

object ErrorManagement {
  type Error = String
  type ErrorList = NonEmptyList[Error]
  type ValidationResult[A] = ValidatedNel[Error, A]
  type ApplicationResult[A] = Either[ErrorList, A]

  def notEmptyString(errorMessage: String)(value: String): ValidationResult[String] =
    if (value == null || "".equals(value))
      errorMessage.invalidNel
    else
      value.validNel

  def dateInThePastOrToday(errorMessage: String)(date: Date): ValidationResult[Date] = {
    if (date == null || date.after(Calendar.getInstance.getTime))
      errorMessage.invalidNel
    else
      date.validNel
  }

  def notNull[T](errorMessage: String)(value: T) : ValidationResult[T] = {
    if (value == null)
      errorMessage.invalidNel
    else
      value.validNel
  }

  def nonEmptyList[T](errorMessage: String)(list: List[T]): ValidationResult[List[T]] =
    list match {
      case _ :: _ => list.validNel
      case _ => errorMessage.invalidNel
    }

  def valid[T](value: T): ValidationResult[T] = value.validNel

  object ErrorList {
    def of(error: Error) : ErrorList = NonEmptyList.of(error)
  }

  object implicits {
    implicit class OptionToEither[T](val option: Option[T]) extends AnyVal {
      def orError(error: Error): Either[ErrorList, T] = option match {
        case None => Left(ErrorList.of(error))
        case Some(x) => Right(x)
      }
    }
  }
}
