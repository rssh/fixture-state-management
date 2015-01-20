/*
 * Copyright 2012-2015 Ruslan Shevchenko
 * Copyright 2012 GradSoft Ltd
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

package ua.gradsoft.managedfixture


sealed trait FixtureStateChange[+State]

case object SameState extends FixtureStateChange[Nothing]

case class NewState[State](val state: State) extends FixtureStateChange[State]

case object UndefinedState extends FixtureStateChange[Nothing]


// vim: set ts=4 sw=4 et:
