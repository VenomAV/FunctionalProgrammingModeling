package Expenses.ApplicationServices

import Expenses.Model.Employee.EmployeeId
import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model.{Expense, ExpenseSheet, OpenExpenseSheet}
import Expenses.Repositories._
import Expenses.Services.ExpenseService
import Expenses.Utils.ErrorManagement
import Expenses.Utils.ErrorManagement.implicits._
import Expenses.Utils.ErrorManagement.{ApplicationResult, ErrorList, ValidationResult}
import cats._
import cats.data.{EitherT, NonEmptyList}
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._

object ExpenseApplicationService {
  def openFor[F[_]](id: EmployeeId)
                   (implicit M:Monad[F],
                    er: EmployeeRepository[F],
                    esr: ExpenseSheetRepository[F]) : F[ApplicationResult[Unit]] =
    (for {
      employee <- er.get(id).toEitherT
      openExpenseSheet <- ExpenseService.openFor(employee).toEitherT[F]
      result <- esr.save(openExpenseSheet).toEitherT
    } yield result).value

  def addExpenseTo[F[_]](expense: Expense, id: ExpenseSheetId)
                        (implicit M:Monad[F],
                         esr: ExpenseSheetRepository[F]) : F[ApplicationResult[Unit]] =
    (for {
      openExpenseSheet <- getOpenExpenseSheet[F](id)
      newOpenExpenseSheet <- ExpenseService.addExpenseTo(expense, openExpenseSheet).toEitherT[F]
      result <- esr.save(newOpenExpenseSheet).toEitherT
    } yield result).value

  def claim[F[_]](id: ExpenseSheetId)
                 (implicit M:Monad[F],
                  esr: ExpenseSheetRepository[F],
                  cr: ClaimRepository[F]) : F[ApplicationResult[Unit]] =
    (for {
      openExpenseSheet <- getOpenExpenseSheet[F](id)
      pair <- ExpenseService.claim(openExpenseSheet).toEitherT[F]
      (claimedExpenseSheet, pendingClaim) = pair
      _ <- esr.save(claimedExpenseSheet).toEitherT
      _ <- cr.save(pendingClaim).toEitherT
    } yield ()).value

  private def getOpenExpenseSheet[F[_]](id: ExpenseSheetId)
                                       (implicit M:Monad[F],
                                        esr: ExpenseSheetRepository[F]): EitherT[F, ErrorList, OpenExpenseSheet] =
    for {
      expenseSheet <- esr.get(id).toEitherT
      openExpenseSheet <- toOpenExpenseSheet(expenseSheet).toEitherT[F]
    } yield openExpenseSheet

  private def toOpenExpenseSheet(es: ExpenseSheet) : ApplicationResult[OpenExpenseSheet] = es match {
    case b: OpenExpenseSheet => Right(b)
    case _ => Left(ErrorList.of(s"${es.id} is not an open expense sheet"))
  }
}

object ExpenseApplicationServiceME {
  def openFor[F[_]](id: EmployeeId)
                   (implicit ME:MonadError[F, Throwable],
                    er: EmployeeRepositoryME[F],
                    esr: ExpenseSheetRepositoryME[F]) : F[Unit] =
    for {
      employee <- er.get(id)
      openExpenseSheet <- liftValidationResult(ExpenseService.openFor(employee))(ME)
      result <- esr.save(openExpenseSheet)
    } yield result

  def addExpenseTo[F[_]](expense: Expense, id: ExpenseSheetId)
                        (implicit ME:MonadError[F, Throwable],
                         esr: ExpenseSheetRepositoryME[F]) : F[Unit] =
    for {
      openExpenseSheet <- getOpenExpenseSheet[F](id)
      newOpenExpenseSheet <- liftValidationResult(ExpenseService.addExpenseTo(expense, openExpenseSheet))(ME)
      result <- esr.save(newOpenExpenseSheet)
    } yield result

  def claim[F[_]](id: ExpenseSheetId)
                 (implicit ME:MonadError[F, Throwable],
                  esr: ExpenseSheetRepositoryME[F],
                  cr: ClaimRepositoryME[F]) : F[Unit] =
    for {
      openExpenseSheet <- getOpenExpenseSheet[F](id)
      pair <- liftValidationResult(ExpenseService.claim(openExpenseSheet))(ME)
      (claimedExpenseSheet, pendingClaim) = pair
      _ <- esr.save(claimedExpenseSheet)
      _ <- cr.save(pendingClaim)
    } yield ()

  private def getOpenExpenseSheet[F[_]](id: ExpenseSheetId)
                                       (implicit ME:MonadError[F, Throwable],
                                        esr: ExpenseSheetRepositoryME[F]): F[OpenExpenseSheet] =
    for {
      expenseSheet <- esr.get(id)
      openExpenseSheet <- toOpenExpenseSheet(expenseSheet)(ME)
    } yield openExpenseSheet

  private def toOpenExpenseSheet[F[_]](es: ExpenseSheet)(implicit ME:MonadError[F, Throwable]) : F[OpenExpenseSheet] = es match {
    case b: OpenExpenseSheet => ME.pure(b)
    case _ => ME.raiseError(new Error(s"${es.id} is not an open expense sheet"))
  }

  private def liftValidationResult[F[_], A](vr: ValidationResult[A])(implicit ME:MonadError[F, Throwable]) : F[A] =
    vr match {
      case Valid(x) => ME.pure(x)
      case Invalid(nel: NonEmptyList[ErrorManagement.Error]) => ME.raiseError(new Error(nel.toList.mkString(", ")))
    }
}