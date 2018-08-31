package Expenses.Repositories

import Expenses.Model.{Employee, EmployeeId}

trait EmployeeRepository[F[_]] {
  def get(id: EmployeeId) : F[Employee]
  def save(employee: Employee): F[Unit]
}
