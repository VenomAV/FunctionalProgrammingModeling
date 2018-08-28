package Infrastructure.Repositories

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.EmployeeRepository
import Expenses.Utils.ErrorManagement.{ApplicationResult, ErrorList}
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import cats.implicits._
import Expenses.Utils.ErrorManagement.implicits._
import doobie.util.invariant.UnexpectedEnd

class DoobieEmployeeRepository extends EmployeeRepository[ConnectionIO] {
  override def get(id: EmployeeId): ConnectionIO[ApplicationResult[Employee]] =
    sql"select * from employees where id=$id".query[Employee]
      .unique
      .attempt
      .map(_.leftMap({
        case UnexpectedEnd => ErrorList.of(s"Unable to find employee $id")
        case x => x.toError
      }))

  override def save(employee: Employee): ConnectionIO[ApplicationResult[Unit]] =
    sql"insert into employees (id, name, surname) values (${employee.id}, ${employee.name}, ${employee.surname})"
      .update.run.attempt.map(_.map(_ =>()).leftMap(_.toError))
}
