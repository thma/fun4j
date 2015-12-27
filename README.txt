Fun4J Readme

Welcome to the 1.0 release of fun4j!

fun4j brings functional programming to the JVM. 
It's a framework that integrates major concepts from functional programming into the Java Platform. 
It also provides seamless integration of Java with Lisp coding, by using a lisp-to-bytecode compiler.

We are now almost feature complete. There's only one failing unit test regarding mutual recursion with higher-order lambdas.

List of changes since release 0.9.1
188	27.12.10 10:34	1	thma	FIX: Compiler maintains uniqueness of compiled class names.
187	23.12.10 18:41	6	thma	FIX: Compiler maintains uniqueness of compiled class names.
186	09.10.10 22:29	2	thma	upgrading to 0.9.2
185	22.09.10 22:20	1	thma	FIX: update project links to SF.net
184	21.09.10 22:10	8	thma	FIX: handling of lambda-terms as parameters
183	20.09.10 18:08	14	thma	Refactoring: extract pre-compiler to a separate class. Now the Compiler doesn't know anything about LISP but only about AST Expressions. This way it can easily used by users to compile their own functional languages.
182	19.09.10 11:52	1	thma	Adding a motivation section to the front page
181	19.09.10 10:11	10	thma	cleaned up compilation exceptions
180	19.09.10 10:10	1	thma	new feature: add a new static method fn(String lambdaTerm) that allows to define Functions based on lisp code with minimal coding overhead in user code, like in this example:  Function add = fn("(lambda (x y) (+ x y))");  cleaned up compilation exceptions
179	17.09.10 22:58	1	thma	letrec example (does not yet work properly...)
178	17.09.10 22:57	2	thma	clean up testcases
177	17.09.10 22:56	5	thma	FIX: clean partial application stuff
176	17.09.10 22:56	1	thma	update docs
175	17.09.10 18:00	3	thma	update docs
174	14.09.10 18:47	3	thma	new feature: function composition
173	10.09.10 17:46	2	thma	new feature: mutual recursion
172	08.09.10 21:22	1	thma	some more examples for lazy evaluation
171	08.09.10 21:21	1	thma	TODO: added some examples for things that should be working asap:  - mutual recursion (quick fix would be to allow deferred lookup of function definitions, i.e dynamic scoping.) - proper handling of closures
170	08.09.10 21:18	1	thma	cosmetics
169	08.09.10 21:18	1	thma	cosmetics
168	08.09.10 21:17	1	thma	FIX: repaired handling of BigDecimals, cleaned up handling of lambdas and macros
167	08.09.10 21:17	1	thma	TODO: fix section on partial application
166	26.08.10 21:19	1	thma	update todo
165	26.08.10 21:19	1	thma	work in progress: Lazy evaluation for implementing nonstrict Combinators like Y & Co.
164	26.08.10 21:17	2	thma	new feature: added parse(lispterm) to Template API
163	26.08.10 21:17	1	thma	new feature: added parse(lispterm) to Template API
162	25.08.10 22:39	3	thma	work in progress: Lazy evaluation for implementing nonstrict Combinators like Y & Co.
161	25.08.10 22:38	3	thma	defined Cons.equals ()
160	25.08.10 21:38	1	thma	unit testCase for runFile
159	25.08.10 21:36	1	thma	new feature: static binding
158	25.08.10 21:36	2	thma	new feature: static binding
157	25.08.10 21:34	2	thma	Extending Serializable is not required right now 
