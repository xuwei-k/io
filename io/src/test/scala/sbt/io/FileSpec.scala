/*
 * sbt IO
 *
 * Copyright 2011 - 2019, Lightbend, Inc.
 * Copyright 2008 - 2010, Mark Harrah
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 */

package sbt.io

import org.scalatest._
import sbt.io.syntax._
import java.nio.file.attribute.PosixFilePermission
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FileSpec extends AnyFlatSpec with Matchers {
  "files" should "set/unset permissions" in {
    IO.withTemporaryDirectory { dir =>
      val t1 = dir / "foo.txt"
      IO.write(t1, "foo")
      if (IO.isPosix) {
        t1.permissions(PosixFilePermission.OWNER_EXECUTE) shouldBe false

        t1.addPermission(PosixFilePermission.OWNER_EXECUTE)
        t1.addPermission(PosixFilePermission.GROUP_WRITE)
        t1.testPermission(PosixFilePermission.OWNER_EXECUTE) shouldBe true
        t1.permissionsAsString should fullyMatch regex "..x.w...."

        t1.removePermission(PosixFilePermission.OWNER_EXECUTE)
        t1.isOwnerExecutable shouldBe false
        t1.permissionsAsString should fullyMatch regex "..-.w...."
      } else ()
    }
  }
}
