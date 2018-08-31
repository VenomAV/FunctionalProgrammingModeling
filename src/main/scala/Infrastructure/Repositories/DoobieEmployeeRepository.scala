package Infrastructure.Repositories

import Expenses.Model.{Employee, EmployeeId}
import Expenses.Repositories.EmployeeRepository
import cats.MonadError
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.invariant.UnexpectedEnd

class DoobieEmployeeRepository(implicit ME: MonadError[ConnectionIO, Throwable]) extends EmployeeRepository[ConnectionIO] {
  override def get(id: EmployeeId): ConnectionIO[Employee] =
    sql"select * from employees where id=$id".query[Employee]
      .unique
      .recoverWith({
        case UnexpectedEnd => ME.raiseError(new Error(s"Unable to find employee $id"))
      })

  override def save(employee: Employee): ConnectionIO[Unit] =
    sql"insert into employees (id, name, surname) values (${employee.id}, ${employee.name}, ${employee.surname})"
      .update.run.map(_ => ())
}
