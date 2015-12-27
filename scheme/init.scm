;; using BigInteger arithmetics is the default. But you may set it to false for performance reasons
;;(bigints false)

;; if set to true the compiler writes a Java class file for each compiled expression, sometimes useful for debugging
;;(debug-enable true)
;; 
;;(define (*compile-hook* form) (print (cons 'compiling: (cons form nil))) )
	
;;(define (*macro-expand-hook* form) (print (cons 'macro_expands_to: (cons form nil))))

;;(define (*define-hook* key value) (print (cons 'define: (cons key (cons value nil)))))

;; define cxxxr and cxxxxr list operations
(define (caaar x) (car (caar x)))
(define (caadr x) (car (cadr x)))
(define (cadar x) (car (cdar x)))
(define (caddr x) (car (cddr x)))
(define (cdaar x) (cdr (caar x)))
(define (cdadr x) (cdr (cadr x)))
(define (cddar x) (cdr (cdar x)))
(define (cdddr x) (cdr (cddr x)))
(define (caaaar x) (caar (caar x)))
(define (caaadr x) (caar (cadr x)))
(define (caadar x) (caar (cdar x)))
(define (caaddr x) (caar (cddr x)))
(define (cadaar x) (cadr (caar x)))
(define (cadadr x) (cadr (cadr x)))
(define (caddar x) (cadr (cdar x)))
(define (cadddr x) (cadr (cddr x)))
(define (cdaaar x) (cdar (caar x)))
(define (cdaadr x) (cdar (cadr x)))
(define (cdadar x) (cdar (cdar x)))
(define (cdaddr x) (cdar (cddr x)))
(define (cddaar x) (cddr (caar x)))
(define (cddadr x) (cddr (cadr x)))
(define (cdddar x) (cddr (cdar x)))
(define (cddddr x) (cddr (cddr x))) 

(define (first x) (car x))
(define (second x) (cadr x))
(define (rest x) (cdr x))
 
;; explicit compile  
(define (cf symbol)
  (compile (cons 'define (cons symbol (cons (lookup symbol) nil)))))

(define (pair? x) (and-primitive (not (null? x)) (list? x)))
(define (nlist n) (if (zero? n) '(0) (cons n (nlist (sub1 n)))))

(define (last lst) 
  (if (null? lst)
  	nil
  	(if (null? (cdr lst)) 
  	  (car lst)
  	  (last (cdr lst))))

;; The dot operator '.' allows to invoke java methods as in (. 'java.lang.Math 'random)
;; It can also be used to access fields. Works for instances and classes !
;; define a random function based on java.lang.Math.random()
(define (rnd) (. 'Math 'random)))
;; define Java based hash function
;;(define hash (javafunction 'org.fun4j.functions.Hash))  

;;; set operations
(define (member-boolean x list) 
  (if (null? list) false
      (if (eqv? x (car list)) true
          (member-boolean x (cdr list)))))

(define (union x y)
  (if (null? x) y
      (if (member (car x) y) (union (cdr x) y)
          (cons (car x) (union (cdr x) y)))))

(define (intersection x y)
  (if (null? x) nil
      (if (member (car x) y) (cons (car x) (intersection (cdr x) y))
          (intersection (cdr x) y)))) 

(define (length list) (if (null? list) 0 (add1 (length (cdr list)))))
(define (nth n list) (if (= 1 n) (car list) (nth (sub1 n) (cdr list))))
(define (append a b) (if (null? a) b (cons (car a) (append (cdr a) b))))
(define (reverse list) (if (null? list) nil (cons (reverse (cdr list)) (car list)))) 

;; define list using varargs syntax
(define (list . elements) elements)

(define (foldright fun acc list) 
	(if (null? list) 
		acc 
		(fun (hd list) (foldright fun acc (tl list)))))
		
(define (foldr f x lst)
     (if (null? lst)
          x
          (foldr f (f x (car lst)) (cdr lst))))		
(define (foldleft fun acc list) (if (null? list) acc (foldleft fun (fun acc (hd list)) (tl list))))

;; tracing facility
(define trace (macro (fun) (list 'trace-primitive (list 'quote fun))))
(define untrace (macro (fun) (list 'untrace-primitive (list 'quote fun))))
(define (tracing onOff)
  (begin 
	(if onOff
		(map (lambda (pair) (if (procedure? (cdr pair)) (trace-primitive (car pair)) nil)) (interaction-environment))
		(map (lambda (pair) (if (procedure? (cdr pair)) (untrace-primitive (car pair)) nil)) (interaction-environment)))
	onOff))
 
(define and (macro args
  (if (null? args)
  	'#t
	(if (null? (cdr args)) 
      (car args)
	  (list 'if (car args) (list 'and (cdr args)) '#f)))))

	    
(define or (macro args
  (if (null? args)
  	'#f
	(if (null? (cdr args)) 
      (car args)
	  (list 'if (car args) '#t (list 'or (cdr args)))))))	  

(define cond (macro clauses 
    (build-if-from clauses)))		
(define (build-if-from clauses)	
	(if (null? clauses) nil
		(append 
			(cons 'if (car clauses))
			(if (null? (cdr clauses)) '(nil)
				(if (eqv? 'else (caadr clauses))
					(cdadr clauses)
					(cons (build-if-from (cdr clauses)) nil))))))	


;; The following quasiquote macro is due to Eric S. Tiedemann.
;;   Copyright 1988 by Eric S. Tiedemann; all rights reserved.
;;
;; Subsequently modified to handle vectors: D. Souflis
;; ported to fun4j: Th. Mahler
(define (foo level form)
     (cond 
		((not (pair? form))
               (if (or-primitive (number? form) (string? form))
                    form
                    (list 'quote form)))
           ((eq? 'quasiquote (car form))
            (mcons form '(quote quasiquote (foo (+ level 1) (cdr form)))))
           (else (if (zero? level)
                   (cond ((eq? (car form) 'unquote) (car (cdr form)))
                         ((eq? (car form) 'unquote-splicing)
                          (error "Unquote-splicing wasn't in a list:"
                                 form))
                         ((and-primitive (pair? (car form))
                               (eq? (car (car form)) 'unquote-splicing))
                          (mappend form (car (cdr (car form)))
                                   (foo level (cdr form))))
                         (else (mcons form (foo level (car form))
                                         (foo level (cdr form)))))
                   (cond ((eq? (car form) 'unquote)
                          (mcons form '(quote unquote (foo (- level 1)
                                                     (cdr form)))))
                         ((eq? (car form) 'unquote-splicing)
                          (mcons form ''unquote-splicing
                                      (foo (- level 1) (cdr form))))
                         (else (mcons form (foo level (car form))
                                         (foo level (cdr form)))))))))

										 
(define (mcons form l r)
	(if (and (pair? form) (pair? r) (pair? l)) 
	 (if (and (pair? (cdr r)) (pair? (cdr l)))
	  (if (and
			(eq? (car r) 'quote)			
			(eq? (cadr r) (cdr form))			
			(eq? (car l) 'quote)			
			(eq? (cadr l) (car form)))
		(if (or-primitive (number? form) (string? form))
		   form
		   (list 'quote form))
		(list 'cons l r))
	(list 'cons l r))
	(list 'cons l r)))
		
(define (mappend form l r)
	(if (or (null? (cdr form))
			(and (pair? r)
				(eq? (car r) 'quote)
				(eq? (car (cdr r)) '())))
		l
		(list 'append l r)))
		
(define quasiquote (macro (l)
   (foo 0 l)))
 
 
(define (extractVars binding) 
    (if (null? binding) 
        nil
        (cons (caar binding) (extractVars (cdr binding)))))

(define (extractValues binding) 
    (if (null? binding) 
        nil
        (cons (car (cdar binding)) (extractValues (cdr binding)))))    
 
;; let macro example: 
;; (let ((a 1)(b 2)) (+ b a)) ==> ((lambda (a b) (+ b a)) 1 2)
(define let (macro (binding body)
  `((lambda ,(extractVars binding) ,body)
     ,@(extractValues binding)))) 

(define (firsts L) (if (null? L) nil (cons (first (first L)) (firsts (rest L)))))
(define (rests L) (if (null? L) nil (cons (rest  (first L)) (rests  (rest L)))))

(define (map . fun-lists)   
   (if (null? (first (rest fun-lists))) nil
        (cons (apply (first fun-lists) (firsts (rest fun-lists)))
              (map (cons (first fun-lists) (rests (rest fun-lists)))))))
	 
;; letrec macro, example:
;;(letrec
;;	((even? (lambda (n) (if (zero? n) true (odd? (sub1 n)))))
;;	 (odd? (lambda (n) (if (zero? n) false (even? (sub1 n))))))
;;	(odd? 37))
;; macro expands to ==>
;;(let 
;;  ((even? (lambda (n even? odd?) (if (zero? n) true (odd? (sub1 n) even? odd?))))
;;   (odd? (lambda (n even? odd?) (if (zero? n) false (even? (sub1 n) even? odd?)))))
;;  (odd? 37 even? odd?))
;; and thus finally to ==>
;;((lambda (even? odd?) (odd? 37 even? odd?)) 
;;	(lambda (n even? odd?) (if (zero? n) true (odd? (sub1 n) even? odd?)))
;;	(lambda (n even? odd?) (if (zero? n) false (even? (sub1 n) even? odd?))))   

(define letrec (macro (bindings body)
  (let 
     ((vars (map first bindings))
	  (vals (map second bindings)))
    `(let ,(map (lambda (var val) `(,var ,(addParams val vars))) vars vals)
		,(addParamsToBody body vars)))))
		
(define (addParams term params) 
  (if (and (list? term) (eqv? 'lambda (first term)))
	(list 'lambda 
		(addParamsToBinding (cadr term) params) 
		(addParamsToBody (caddr term) params))
	term))

(define (addParamsToBinding vars params) 
	(append vars params))
	
(define (addParamsToBody body vars)
  (cond 
    ((not (list? body)) body)
	((member-boolean (car body) vars)
	  (append (list (car body) (map (lambda (term) (addParamsToBody term vars)) (cdr body))) vars))
	((list? (car body))
	  (map (lambda (term) (addParamsToBody term vars)) body))
	(else 
	  (cons (car body) (map (lambda (term) (addParamsToBody term vars)) (cdr body))))))
	  
(define (addParamsToBody body vars)
  (if (not (list? body)) body
  (if (member-boolean (car body) vars)
	  (append (list (car body) (map (lambda (term) (addParamsToBody1 term vars)) (cdr body))) vars)
  (if (list? (car body))
	  (map (lambda (term) (addParamsToBody1 term vars)) body)
	  (cons (car body) (map (lambda (term) (addParamsToBody1 term vars)) (cdr body)))))))	  

(define (addParamsToBody1 body vars)
  (addParamsToBody body vars))
  
(define (test a b) 
  (map (lambda (n) (* n b)) a))  
	  
(define term '(lambda (n) (if (zero? n) true (odd? (sub1 n)))))
(define params '(even? odd?))       
   
;; Scheme math functions   
(define (complex? x) (number? x))
(define rational? real?)
(define (inexact? x) (and (real? x) (not (exact? x))))
(define (remainder a b) (% a b))
(define (even? n) (zero? (remainder n 2)))
(define (odd? n) (not (zero? (remainder n 2))))
(define (< x y) (> y x))
(define (positive? n) (> n 0))
(define (negative? n) (< n 0))

(define (abs n) (if (>= n 0) n (* -1 n)))
(define (exact->inexact n) (* n 1.0))
(define (<> n1 n2) (not (= n1 n2)))
(define (max . lst)
	(foldr (lambda (a b) (if (> a b) a b)) (car lst) (cdr lst)))
(define (min . lst)
    (foldr (lambda (a b) (if (< a b) a b)) (car lst) (cdr lst)))
(define (succ x) (+ x 1))
(define (pred x) (- x 1))
(define (quotient a b) (div a b))	

;; greatest common divisor
(define (gcd . a)
    (if (null? a)
      0
      (let ((aa (abs (car a)))
            (bb (abs (cadr a))))
         (if (zero? bb)
              aa
              (gcd bb (% aa bb)))))))	
              
;; least common multiple              	  
(define (lcm . a)
    (if (null? a)
      1
      (let ((aa (abs (car a)))
            (bb (abs (cadr a))))
         (if (or (zero? aa) (zero? bb))
             0
             (abs (* (quotient aa (gcd aa bb)) bb)))))))   
 
 ;; chars and strings
(define (list->string charlist)
	(implode charlist))
(define (string->list s)
	(explode s))
	
(define (string-length s)
	(length (string->list s)))
	
(define (string . charlist)
    (list->string charlist))

(define (string-append . strings)
	(implode strings))
(define (string-copy str)
     (string-append str))

(define (string->atom string)
	(let ((result (parse string)))
		(if (atom? result) result (error "string->atom: not an atom" result))))

(define (string->anyatom str pred)
    (let ((a (string->atom str)))
		(if (pred a) a (error "string->xxx: not a xxx" a))))

(define (string->number str) (string->anyatom str number?))

(define (atom->string a)
	(implode (explode a)))

(define (anyatom->string n pred)
  (if (pred n)
      (atom->string n)
      (error "xxx->string: not a xxx" n)))

(define (number->string n) (anyatom->string n number?))		

(define (boolean? x)
	(if (or (eq x true) (eq x false)) true false))
	
	
(define (equal? x y)
  (cond
    ((pair? x)
      (and (pair? y)
           (equal? (car x) (car y))
           (equal? (cdr x) (cdr y))))
    ((vector? x)
      (and (vector? y) (vector-equal? x y)))
    ((string? x)
      (and (string? y) (string=? x y)))
    (else (eqv? x y))))
	
(define (vector-equal? x y) 
  (eqv? x y))

(define (string=? x y) 
  (eqv? x y))

;;;; generic-member
(define (generic-member cmp obj lst)
  (cond
    ((null? lst) #f)
    ((cmp obj (car lst)) lst)
    (else (generic-member cmp obj (cdr lst)))))

(define (memq obj lst)
     (generic-member eq? obj lst))
(define (memv obj lst)
     (generic-member eqv? obj lst))
(define (member obj lst)
     (generic-member equal? obj lst))

;;;; generic-assoc
(define (generic-assoc cmp obj alst)
     (cond
          ((null? alst) #f)
          ((cmp obj (caar alst)) (car alst))
          (else (generic-assoc cmp obj (cdr alst)))))

(define (assq obj alst)
     (generic-assoc eq? obj alst))
(define (assv obj alst)
     (generic-assoc eqv? obj alst))
(define (assoc obj alst)
     (generic-assoc equal? obj alst))

(define (acons x y z) (cons (cons x y) z))

;; macro unless
(define unless (macro form
     `(if (not ,(car form)) (begin ,@(cdr form)) nil)))

;; macro when
(define when (macro form
     `(if ,(car form) (begin ,@(cdr form)) nil)))									 
	
;;(let* ((a 3) (b (* a 2)) (c (+ a b))) (list a b c))
;; expands to:	
;;(let ((a 3)) (let ((b (* a 2))) (let ((c (+ a b))) (list a b c))))	
;; let* macro: build as a recursive sequence of lets:
(define let* 
	(macro (bindings body)
		(build-lets bindings body)))
		
(define (build-lets bindings body)
	(if (null? bindings) body
		`(let (,(car bindings)) ,(build-lets (cdr bindings) body))))
	
;; Random number generator (maximum cycle)
(define (modulo m a) (% m a))
	
(define *seed* 1)
(define (random-next)
     (let* ((a 16807) (m 2147483647) (q (quotient m a)) (r (modulo m a)))
	   (begin
          (set! *seed*
               (-   (* a (- *seed*
                         (* (quotient *seed* q) q)))
                    (* (quotient *seed* q) r)))
          (if (< *seed* 0) (set! *seed* (+ *seed* m)) nil)
          *seed*)))
  
EOF