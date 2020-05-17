/*
 * Copyright 2020 Precog Data
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package quasar.blobstore.azure

import quasar.blobstore.azure.testImplicits._

import java.lang.{String, Throwable}
import scala.util.Either

import cats.effect.{ContextShift, Effect, IO}
import cats.effect.concurrent.Deferred
import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment

class EffectfulSpec extends Specification {
  def unsafeRunEffect[F[_]: Effect, A](fa: F[A])(implicit cs: ContextShift[IO]): A = {
    val ioa = for {
      d <- Deferred[IO, Either[Throwable, A]]
      _ <- Effect[F].runAsync(fa)(d.complete(_)).to[IO]
      r <- d.get
      a <- IO.fromEither(r)
    } yield a

    ioa.unsafeRunSync()
  }

  implicit class RunExample(s: String) {
    def >>*[A: AsResult](fa: => IO[A]): Fragment =
      s >> unsafeRunEffect(fa)
  }
}
