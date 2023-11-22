# Programming Assignment 5: Code Generation

-----

### Date Out: Tuesday, November 21
### Due Date: Monday, December 11 (midnight!)

-----

## Introduction

In this programming assignment, we will traverse our AST and emit assembly code
that can be loaded and run in the Mips Simulator, MARS! After completing this
assignment, you will now have a complete compiler (albeit a lightweight one) that
goes all the way from source code written in Ram23, all the way to machine code that
is executable!

### Task 1:

Download the starter code from your repo. Here are the new/important file changes
compared to the last project:

```
MyCompilerProject
  - src/main/java/compilers/Ram23Compiler.java        (updated driver for code generation at the very bottom)
  - src/main/java/visitor/CodeGenerator.java          (extends DepthFirstVisitor. This is the file you will primarily be modifying.)
  - test/java/compilers/CodegenTest.java              (used by mvn test to run your project against all tests -- this is a bit CPU and time heavy)
  - test/java/compilers/CodegenTestSingle.java        (used only in GH to output a numeric score to GHC)
  - test/java/compilers/test_programs/*.ram23         (source test programs)
  - test/java/compilers/test_programs/*.correct       (the correct output of the source test programs)  
  - lib/Mars4_5.jar                                   (MARS Mips Simulator)
```

* `CodeGenertor.java` is the primary file that you will complete. Along the way you will
  test your implementation against my test programs, as well as others that you create.
  The driver file will create `.s` mips assembly files in the same directory as the test
  programs.
* Look at `CodeGenerator.java`. I already give you a few methods which can be used to
  emit assembly code, labels, and comments. In general, you will implement a `visit` method
  for each of the following AST nodes. Each method should emit appropriate MIPS assembly
  language code. The code will be judged on correctness and that its performance is
  "reasonable" (that it is not blatantly inefficient).
* Use the `Ram23Compiler.java` driver to run your code generator against
  a single .ram23 file. The assembly output will be written to stdout, which you can copy/paste or
  save into a .s assembly file. Then manually load this file into MARS and see if things are working
  like you expect.
* *Simplifications:* In this project, we are going to heavily utilize the stack,
  rather than only registers, because we have not yet discussed register allocation techniques.
  Though our programs will take a performance hit in speed, they will be guaranteed to
  execute correctly!
* Which MIPS instructions do you need to use in this project? My program (only?) uses the
  following instructions: `subu, sw, addi, lr, jr, move, syscall, add, sub, mul, slt, xori, li, j, beqz, jal, la`

### Task 2: Program, MainClass

These methods are already provided in the starter code, and are more complex that the initial methods that you are
asked to implement. These methods are called near the beginning of an AST traversal.
`Program` emits the `.text` directive, and traverses deeper into the main class and the
other classes in the program. `MainClass` sets the `currClass` and `currMethod` fields
(in an ugly way, given the available public methods in the symbol table data structure from
the previous homework), emits code to establish main's prologue (allocating space on the stack,
setting up the frame pointer, storing the return address, etc.), traverses into the statement,
and emits code to establish main's epilogue.

- You can follow any stack frame layout as you wish. Everything will work fine as long as you
  are consistent. However, I recommend following Hennessy's convention on page A-6 (which is
  the one we have been using in class.)
- You should be able to run my starter code to produce an "empty program": mips assembly
  with the `.text` directive that pushes and pops the stack. The program is boring, but it
  should execute in MARS.


### Task 3: IntegerLiteral, Print, Println

Now it's time for you to code! Implement `visit` methods for these three nodes.

Ok, I actually give `IntegerLiteral` to you also, which emits a single instruction that
loads the the node's int literal into the register `$v0`. **For all expressions, use the
convention that assumes that the result of the expression will always be placed in
register `$v0`.**

`Print` method - Emit the statements necessary to have mips print out the expression's value:

1. call the `accept` method on the expression (which jumps to `IntegerLiteral` and emits code for the literal value to be placed in register `$v0`)
1. move the value in `$v0` to `$a0`
1. load the literal `1` into `$v0`
1. emit a `syscall` command (observe how these last three commands follow the steps for MIPS to print an integer (pg. A-48)

Test your code on the program `Print.ram`

`Println` is a little tricky because you also have to emit a system call code for
print the "\n" newline string. Example code which should point you in the right direction
is also provided on page A-48. You will have to emit `.data` directive code, a label,
and a `.asciiz` directive which takes a string literal and stores it in memory (null-terminated).
(Where should this code be emitted?) Test your code on the program `Println.ram`

### Task 4: Plus, Minus, Times

Our compiled code will operate as a stack machine because we are not sure which registers are
safe to use. To emit code for `Plus`:

1. evaluate the left subexpression by calling the `accept` method on it (Where do we assume its return value is placed?)
1. push this value onto the stack (this involves two instructions)
1. evaluate the right subexpression
1. pop the value on the top of the stack (which is the left subexpression) into register `$v1`
1. emit an `add` instruction (Where should the result be placed?)

Test your code on `Plus.ram`. Create your own more advanced test cases that have
parenthesized expressions. Also implement the `visit` methods for `Minus` and `Times`.

### Task 5: True, False

One liners. Internally represent `true` and `false`, as `1` and `0`, respectively.
These methods are very similar to `IntegerLiteral`.

### Task 6: If

Follow these steps which are similar to the code of Hennessy's factorial example on page A-28:

1. evaluate the expression
1. if the result is false, branch to label "IfFalse"
1. execute the "true" statement block
1. jump to label "IsDone"
1. emit label "IfFalse"
1. execute the "false" statement block
1. emit label "IsDone"

However, an issue will arise with these labels if there are multiple If statements in
the same program. Our labels must be unique, such as "IfFalse4". In my program, I created
an integer counter and appended an integer value to each label, in such a way that each
emitted label was uniquely named. Test your code on `IfTrue.ram` and
`IfFalse.ram`.

### Task 7: And, Or, LessThan, Equals, Not

For the binary logical operators, first evaluate the left subexpression. Remember to
implement short circuit processing (for example, if the LHS of an `And` is false), we do
not evaluate the RHS subexpression). Use techniques similar to what was used for the `If`
node to create labels and utilize branches and jumps.

For the binary relational operators, where should the resulting value of the expression
be stored?

Test your code on `And.ram`, `LessThan.ram`, `Not1.ram`, `Not2.ram`

### Task 8: Call, MethodDecl, ClassDeclSimple

This is the trickiest part of the homework -- and the part where debugging gets difficult.
Utilize the stepper in MARS, the ability of setting breakpoints, altering memory during runtime,
and switching between hex/decimal/binary views of registers and memory.

For `Call`, emit a comment that we are preparing for a method call. Then push onto the
stack the arguments of the call. Where should the stack pointer be, in relation to the arguments?

For `MethodDecl`, allocate onto the stack a new stack frame. Unlike the stack frame
for `MainClass`, this frame will also need to have space for local variables.
* How much additional space do you need? Fortunately, the restrictions of MiniJava/Ram help us here. All local variables are declared at the beginning of a method and are accessible via the `fl` field. So, yes we can calculate the stack frame's size upfront. (Refer to Hennessy's Figure A.11 on where locals should go in the stack frame.)

How many arguments should be pushed onto the stack in `Call`? You have a few options.
Because we want to get data out of registers and onto the stack, you *are* able to
pass arg0 .. arg3 in registers, as long as you store them on the stack
in `MethodDecl`. Or you could adopt the convention that `Call` pushes *all* arguments
onto the stack.
* *How can we access local vars that are stored on the stack?* We need to remember their offset from the frame pointer. This is the necessity of the `RamClass` and `RamMethod` fields, to keep track of the current class and current method in the AST traversal. I modified my symbol table so that each `RamVarible` object also has its `offset` stored. Then, when a variable is referenced (given some class and some method), we can lookup its offset. (This offset will be consistent in all frames that it is in.) Create a `visit` method for `ClassDeclSimple`, and set the `RamClass` field, so that you can use it when grabbing `RamVariable`s from the symbol table.

Note that we are not yet handling class-level variables.

Implement all of this, debug smartly, good luck, and test your code with `NewObject.ram`

### Task 9: Identifier, IdentifierExp, Assign

What is the difference between `Identifier` and `IdentifierExp`?

- `Identifier` returns the address of the given identifier. Compute the identifier's address, by grabbing its offset from the symbol table, and emit code that calculates the offset by adding it to the address of the frame pointer. Where should this result be stored?
- `IdentifierExp` returns the value of the given identifier. Once again, compute the identifier's address, and emit code that calculates the address of the identifier. But rather than returning the address of the identifier, return the value at that address. *Hint:* one additional instruction is necessary compared to Identifier.

For `Assign`, first compute the RHS expression and push the result onto the stack. Then call the accept method for the Identifier field. Now that we have the address of the identifier in a register, pop the stack into register $v1, and emit an instruction to save the value in $v1 into the address of the identifier.

Test your code with `MethodCall.ram`, `Factorial0.ram`, `Factorial1.ram`, and `Factorial10.ram`

### Task 10: While

Follow these steps, similar to `If`:

1. emit a unique label for this while loop
1. evaluate the expression
1. if the expression is false, goto a "done" label
1. execute the statements in the body of the loop
1. perform an unconditional jump back to the loop label
1. emit the "done" label

Test your code with `While.ram`.

-----

**CSC416:** You can stop here!!! In GH, your code will be tested on the complete compiler
that CSC565 is building, but I'll ignore these tests when recording your grade on D2L.
**CSC565:** Keep going... (and also uncomment the additional tests in `CodegenTest.java`)

-----

### Task 11: This, NewObject, ClassDeclSimple, Call

This is very tricky! We also need to handle object instantiations which have
class-level instance variables. Each object will have a base address, and the
data of the object (part of the "object frame") will be stored on the heap.
(Recall that the heap is stored in lower addresses of memory and grows upward toward the stack.)

- Use the following convention: because RAM is object-oriented every method call is called from the context of a class instantiation (this is the `Exp` field in the `Call` AST node).
- Methods may reference class-level instance variables. In RAM, any class-level variables is always an instance variables and is never static. We need to appropriate allocate memory for and keep track of where these instance variables exist in memory (in the heap).
- Here is our convention: let *arg0* (the first argument) in the current activation record always have the address of where the current "object" exists in the heap. Then, when referencing a class-level instance variable, we can use arg0 to get the base address of the "object frame" and then use an offset to calculate where the address of the variable.
- `This` returns the address of the current object. Copy the contents of arg0 in the current activation frame into $v0.
- For `NewObject`, which is called whenever there is a new class instantiation, we are going to allocate space for class-level instance variables in the heap. Calculate the number of instance variables and the needed amount of bytes. Then use the `syscall` system code 9 which calls the sbrk service that allocates a specified amount of memory. The memory address of the freshly allocation space is returned in $v0, which is exactly what we want.
- For `ClassDeclSimple`, if any class-level instance variables are declared via the `vl` field, I set their `offset` to represent where they exist from the object's base address in the heap.
- In each `Call`, be sure that the first argument, arg0, is always the calling object.

### Task 12: Identifier, IdentifierExp

These methods will have to be modified to account for class-level instance variables.
Perform a lookup in the symbol table to determine if the given node references a local
variable/parameter or a class-level variable. Separately handle each case.
If the variable is class-level, use the base address of the current class in the heap
with the offset of where the variable exists in this "object frame".

Test your code with `ClassLevel.ram`. Also step through the simulation to make sure that
the variable addresses are what you expect.

### Task 13: NewArray, ArrayLength, ArrayLookup ArrayAssign

Arrays are tricky because we do not know the their size simply by examining the
initial declaration. Further, new array objects can be created which change the
size of the array at run-time!

- For `NewArray`, use `syscall` system code 9 to allocate memory on the heap. Evaluate the NewArray node expression, which is the number of elements in the array. Calculate the number of necessary bytes. Include an additional word to store the number of elements in the array (by convention, the `.length` is the first word of the array). Emit code that performs the `syscall`. (The address of the allocated memory is returned in $v0.)
- For `ArrayLength`, emit code that figures out where the array is in memory. Return the value of the first word of the array.
- `ArrayLookup` returns the value of the specified array element. First, evaluate the `e1` field which is the identifier of the array. This returns the address of the array (where it resides in the heap). Push this address onto the stack. Second, evaluate the `e2` field which is the array index. Emit code that pops the stack into a register (the address of the array) and calculates the address of the specified array element. Using this address, a pointer to the specified array index, return the value at this memory location in register $v0.
- `ArrayAssign` assigns the value of the expression of field `e2` into the array element represented by the array identifier (field `i`) and array index (field `e1`). First, calculate the base address of the array by evaluating the array identifier. This returns the location of a pointer to the array. Follow this pointer and push the address of the array onto the stack. Second, evaluate the array index expression. Emit code that pops the stack, computes the address of the array element, and pushes this address onto the stack. Third, evaluate the field `e2` expression that we will assign into the array. Once this value is in $v0, pop the array element address into $v1, and emit code to save the value into the memory location.

Test your code on `ArrayTest.ram` and `LinearSearch.ram`

-----

**That's it!!!** You have completed your Ram23 Compiler. ☺ Give yourself a pat on the back for completing
a very complex piece of software that involved many phases: lexical analysis,
parsing, semantic grammars and AST construction, building a symbol table,
type checking and semantic analysis, and code generation.
This is one of the most complex pieces of software that a student writes in a CS program.

## Ram23 Resources

Links to the MiniJava and Ram23 Specification are in the starter code repo.

## Submission instructions

Push your completed Java Maven project to your GitHub. Verify the success of
the autograding GitHub Action.