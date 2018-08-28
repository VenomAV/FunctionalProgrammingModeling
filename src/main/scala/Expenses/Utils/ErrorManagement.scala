package Expenses.Utils

import java.util.{Calendar, Date}

import cats.Monad
import cats.data.{EitherT, NonEmptyList, ValidatedNel}
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
    implicit class FOptionToFEither[F[_], T](val option: F[Option[T]]) extends AnyVal {
      def orError(error: Error)(implicit M:Monad[F]): F[Either[ErrorList, T]] =
        option.map(x => x.orError(error))
      def orErrorT(error: Error)(implicit M:Monad[F]): EitherT[F, ErrorList, T] =
        EitherT(option.orError(error))
    }
    implicit class ValidationResultToEitherT[T](val validationResult: ValidationResult[T]) extends AnyVal {
      def toEitherT[F[_]]()(implicit M:Monad[F]): EitherT[F, ErrorList, T] =
        validationResult.toEither.toEitherT[F]
    }
    implicit class FAnyToEitherT[F[_], T](val any: F[T]) extends AnyVal {
      def rightT()(implicit M:Monad[F]): EitherT[F, ErrorList, T] =
        EitherT.right[ErrorList](any)
    }
    implicit class FEitherToEitherT[F[_], E, T](val any: F[Either[E, T]]) extends AnyVal {
      def toEitherT()(implicit M:Monad[F]): EitherT[F, E, T] = EitherT(any)
    }
    implicit class ThrowableToErrorList(val throwable: Throwable) extends AnyVal {
      def toError : ErrorList = ErrorList.of(throwable.getMessage)
    }
  }
}
