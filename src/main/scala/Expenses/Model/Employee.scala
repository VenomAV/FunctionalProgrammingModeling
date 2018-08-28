package Expenses.Model

import java.util.UUID

import Expenses.Model.Employee.EmployeeId
import Expenses.Utils.ErrorManagement
import Expenses.Utils.ErrorManagement.Validated
import cats.implicits._

sealed case class Employee private (id : EmployeeId, name: String, surname: String)

object Employee {
  type EmployeeId = UUID

  private val validateId = ErrorManagement.notNull[EmployeeId]("id is null")(_)
  private val validateName = ErrorManagement.notEmptyString("name is empty")(_)
  private val validateSurname = ErrorManagement.notEmptyString("surname is empty")(_)

  def create(id: EmployeeId, name: String, surname: String) : Validated[Employee] =
    (validateId(id), validateName(name), validateSurname(surname))
      .mapN(new Employee(_, _, _))

  def create(name: String, surname: String) : Validated[Employee] =
    create(UUID.randomUUID(), name, surname)
}