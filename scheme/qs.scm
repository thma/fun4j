;;quicksort example
;; 

(define append (lambda (a b) (if (null? a) b (cons (hd a) (append (tl a) b)))))

(define gt (lambda (x xs)
  (if (null? xs) 
      nil
      (if (<= (hd xs) x)
          (gt x (tl xs))
          (cons (hd xs) (gt x (tl xs)))))))

(define lt (lambda (x xs)
   (if (null? xs)
       nil
       (if (<= (hd xs) x)
           (cons (hd xs) (lt x (tl xs)))
           (lt x (tl xs))))))

(define qs (lambda (ys)
   (if (null? ys)
       ys
       (append (qs (lt (hd ys) (tl ys)))
               (cons (hd ys)
                     (qs (gt (hd ys) (tl ys))))))))


(print (qs '(5 4 9 2 33 66 1 123 3)))