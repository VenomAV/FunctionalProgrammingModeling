package Expenses.Repositories

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Utils.ErrorManagement.ApplicationResult

trait EmployeeRepository[F[_]] {
  def get(id: EmployeeId) : F[ApplicationResult[Employee]]
  def save(employee: Employee): F[ApplicationResult[Unit]]
}
