package Infrastructure.Repositories

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.EmployeeRepository
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._

class DoobieEmployeeRepository extends EmployeeRepository[ConnectionIO] {
  override def get(id: EmployeeId): ConnectionIO[Option[Employee]] =
    sql"select * from employees where id=$id".query[Employee].option

  override def save(employee: Employee): ConnectionIO[Unit] =
    sql"insert into employees (id, name, surname) values (${employee.id}, ${employee.name}, ${employee.surname})"
      .update.run.map(_ => ())
}
