package Infrastructure

import java.util.UUID

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.EmployeeRepository
import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers}

import scala.collection.mutable.ListBuffer

abstract class EmployeeRepositoryContractTest[F[_]](implicit ME:MonadError[F, Throwable])
  extends FunSpec with Matchers with BeforeAndAfterEach {

  val toBeDeletedEmployeeIds: ListBuffer[EmployeeId] = ListBuffer.empty[EmployeeId]

  describe("get") {
    it("should retrieve existing element") {
      val id = UUID.randomUUID()
      val name = s"A $id"
      val surname = s"V $id"
      val sut = createRepositoryWith(List(Employee(id, name, surname)))

      run(sut.get(id)) should be(Right(Employee(id, name, surname)))
    }
    it("should return error when employee is missing") {
      val id = UUID.randomUUID()
      val sut = createRepositoryWith(List())

      run(sut.get(id)) should matchPattern {
        case Left(ex : Throwable) if ex.getMessage.equals(s"Unable to find employee $id") =>
      }
    }
  }
  describe("save") {
    it("should work") {
      val id = UUID.randomUUID()
      val sut = createRepositoryWith(List())

      toBeDeletedEmployeeIds += id
      run(for {
        _ <- sut.save(Employee(id, s"Andrea $id", s"Vallotti $id"))
        employee <- sut.get(id)
      } yield employee) should matchPattern {
        case Right(Employee(_, _, _)) =>
      }
    }
  }

  override protected def afterEach(): Unit = {
    cleanUp(toBeDeletedEmployeeIds.toList)
  }

  def createRepositoryWith(employees: List[Employee]): EmployeeRepository[F]

  def run[A](toBeExecuted: F[A]) : Either[Throwable, A]

  def cleanUp(employeeIds: List[EmployeeId]): Unit
}

