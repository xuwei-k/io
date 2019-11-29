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

import java.nio.file.{ Files, Path => NioPath }

import org.scalatest._
import Path._
import sbt.io.syntax._
import org.scalatest.flatspec
import org.scalatest.matchers.should.Matchers

class PathMapperSpec extends flatspec.FixtureAnyFlatSpec with Matchers {

  type FixtureParam = NioPath

  "rebase | flat" should "copy resource mappings correctly" in { tempDirectory =>
    val base = tempDirectory.toFile

    val files = Seq(base / "src" / "main" / "resources" / "scalac-plugin.xml")
    val dirs = Seq(
      base / "src" / "main" / "resources",
      base / "target" / "scala-2.11" / "resource_managed" / "main"
    )
    val target = base / "target" / "scala-2.11" / "classes"

    val mappings = (files --- dirs) pair (
        file => rebase(dirs, target)(file) orElse (flat(target): File => Option[File])(file)
    )

    mappings shouldBe Seq(
      base / "src" / "main" / "resources" / "scalac-plugin.xml" ->
        base / "target" / "scala-2.11" / "classes" / "scalac-plugin.xml"
    )
  }

  "directory" should "create mappings including the baseDirectory" in { tempDirectory =>
    val nestedFile1 = Files.createFile(tempDirectory resolve "file1").toFile
    val nestedFile2 = Files.createFile(tempDirectory resolve "file2").toFile
    val nestedDir = Files.createDirectory(tempDirectory resolve "dir1")
    val nestedDirFile = Files.createDirectory(nestedDir resolve "dir1-file1").toFile

    IO.touch(nestedFile1)
    IO.touch(nestedFile2)
    IO.createDirectory(nestedDir.toFile)
    IO.touch(nestedDirFile)

    val mappings = Path.directory(tempDirectory.toFile).map { case (f, s) => (f, file(s)) }

    mappings should contain theSameElementsAs List(
      tempDirectory.toFile -> file(s"${tempDirectory.getFileName}"),
      nestedFile1 -> file(s"${tempDirectory.getFileName}/file1"),
      nestedFile2 -> file(s"${tempDirectory.getFileName}/file2"),
      nestedDir.toFile -> file(s"${tempDirectory.getFileName}/dir1"),
      nestedDirFile -> file(s"${tempDirectory.getFileName}/dir1/dir1-file1")
    )
  }

  it should "create one mapping entry for an empty directory" in { tempDirectory =>
    val mappings = Path.directory(tempDirectory.toFile)

    mappings should contain theSameElementsAs List[(File, String)](
      tempDirectory.toFile -> s"${tempDirectory.getFileName}"
    )
  }

  it should "create an empty mappings sequence for a non-existing directory" in { tempDirectory =>
    val nonExistingDirectory = tempDirectory.resolve("imaginary")
    val mappings = Path.directory(nonExistingDirectory.toFile)

    mappings should be(empty)
  }

  it should "create one mapping entry if the directory is a file" in { tempDirectory =>
    val file = tempDirectory.resolve("file").toFile
    IO.touch(file)
    val mappings = Path.directory(file)

    mappings should contain theSameElementsAs List[(File, String)](
      file -> s"${file.getName}"
    )
  }

  "contentOf" should "create mappings excluding the baseDirectory" in { tempDirectory =>
    val nestedFile1 = Files.createFile(tempDirectory resolve "file1").toFile
    val nestedFile2 = Files.createFile(tempDirectory resolve "file2").toFile
    val nestedDir = Files.createDirectory(tempDirectory resolve "dir1")
    val nestedDirFile = Files.createDirectory(nestedDir resolve "dir1-file1").toFile

    IO.touch(nestedFile1)
    IO.touch(nestedFile2)
    IO.createDirectory(nestedDir.toFile)
    IO.touch(nestedDirFile)

    val mappings = Path.contentOf(tempDirectory.toFile).map { case (f, s) => (f, file(s)) }

    mappings should contain theSameElementsAs List(
      nestedFile1 -> file(s"file1"),
      nestedFile2 -> file(s"file2"),
      nestedDir.toFile -> file(s"dir1"),
      nestedDirFile -> file(s"dir1/dir1-file1")
    )
  }

  it should "create an empty mappings sequence for an empty directory" in { tempDirectory =>
    val mappings = Path.contentOf(tempDirectory.toFile)

    mappings should be(empty)
  }

  it should "create an empty mappings sequence for a non-existing directory" in { tempDirectory =>
    val nonExistingDirectory = tempDirectory.resolve("imaginary")
    val mappings = Path.contentOf(nonExistingDirectory.toFile)

    mappings should be(empty)
  }

  it should "create an empty mappings sequence if the directory is a file" in { tempDirectory =>
    val file = tempDirectory.resolve("file").toFile
    val mappings = Path.contentOf(file)

    mappings should be(empty)
  }

  override protected def withFixture(test: OneArgTest): Outcome = {
    val tmpDir = Files.createTempDirectory("path-mappings")
    try {
      withFixture(test.toNoArgTest(tmpDir))
    } finally {
      // cleanup an delete the temp directory
      IO.delete(tmpDir.toFile)
    }
  }
}
