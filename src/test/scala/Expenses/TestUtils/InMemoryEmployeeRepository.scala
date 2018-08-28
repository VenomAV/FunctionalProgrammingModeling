package Expenses.TestUtils

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.{EmployeeRepository, EmployeeRepositoryME}
import Expenses.TestUtils.AcceptanceTestUtils.{OrError, Test, TestME, TestState}
import Expenses.Utils.ErrorManagement.ApplicationResult
import Expenses.Utils.ErrorManagement.implicits._
import cats.data.StateT._
import cats.data.{State, StateT}
import cats.implicits._

class InMemoryEmployeeRepository extends EmployeeRepository[Test] {
  override def get(id: EmployeeId): Test[ApplicationResult[Employee]] =
    State {
      state => (state, state.employees.find(_.id == id).orError(s"Unable to find employee $id"))
    }

  override def save(employee: Employee): Test[ApplicationResult[Unit]] =
    State {
      state => (state.copy(employees = employee :: state.employees), Right(()))
    }
}

class InMemoryEmployeeMERepository extends EmployeeRepositoryME[TestME] {
  override def get(id: EmployeeId): TestME[Employee] =
    for {
      state <- StateT.get[OrError, TestState]
      result <- state.employees.find(_.id == id) match {
        case Some(x) => pure[OrError, TestState, Employee](x)
        case _ => liftF[OrError, TestState, Employee](Left(new Error(s"Unable to find employee $id")))
      }
    } yield result

  override def save(employee: Employee): TestME[Unit] =
    for {
      _ <- modify[OrError, TestState](state => state.copy(employees = employee :: state.employees))
    } yield ()
}