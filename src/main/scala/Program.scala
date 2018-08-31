import java.util.Date

import Expenses.Model._
import Expenses.Utils.ErrorManagement.implicits._
import Infrastructure.Repositories.{DoobieClaimRepository, DoobieEmployeeRepository, DoobieExpenseSheetRepository}
import Program.ApplicationService._
import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import squants.market.Money

object Program {
  object ApplicationService {
    import Expenses.ApplicationServices.ExpenseApplicationService

    def openFor(id: EmployeeId)
               (implicit er: DoobieEmployeeRepository, esr: DoobieExpenseSheetRepository) : ConnectionIO[ExpenseSheetId] =
      ExpenseApplicationService.openFor[ConnectionIO](id)

    def addExpenseTo(id: ExpenseSheetId, expense: Expense)
                          (implicit esr: DoobieExpenseSheetRepository) : ConnectionIO[Unit] =
      ExpenseApplicationService.addExpenseTo[ConnectionIO](expense, id)

    def claim(id: ExpenseSheetId)
                   (implicit esr: DoobieExpenseSheetRepository,
                    cr: DoobieClaimRepository) : ConnectionIO[ClaimId] =
      ExpenseApplicationService.claim[ConnectionIO](id)
  }

  def main(args: Array[String]): Unit = {
    implicit val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql:postgres",
      "postgres",
      "p4ssw0r#")
    implicit val employeeRepository: DoobieEmployeeRepository = new DoobieEmployeeRepository
    implicit val expenseSheetRepository: DoobieExpenseSheetRepository = new DoobieExpenseSheetRepository
    implicit val claimRepository: DoobieClaimRepository = new DoobieClaimRepository

    val program = for {
      employee <- Employee.create("A", "V").orRaiseError[ConnectionIO]
      _ <- employeeRepository.save(employee)
      openExpenseSheetId <- openFor(employee.id)

      travelExpense <- Expense.createTravel(Money(30, "EUR"), new Date(), "Florence", "Barcelona")
        .orRaiseError[ConnectionIO]
      accommodationExpense <- Expense.createAccommodation(Money(100, "EUR"), new Date(), "Plaça Catalunya")
          .orRaiseError[ConnectionIO]

      _ <- addExpenseTo(openExpenseSheetId, travelExpense)
      _ <- addExpenseTo(openExpenseSheetId, accommodationExpense)

      _ <- claim(openExpenseSheetId)
    } yield ()

    program.transact(xa).unsafeRunSync()
  }
}
