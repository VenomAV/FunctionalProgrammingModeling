package Expenses.Repositories

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId

trait EmployeeRepository[F[_]] {
  def get(id: EmployeeId) : F[Option[Employee]]
  def save(employee: Employee): F[Unit]
}