.globl	main
		.type	main, @function
main:
		pushl	%ebp				/* Stack Frame (Prolog) */
		movl	%esp, %ebp			
		xorl	%eax, %eax			/* %eax = 0 */
		jmp		.L2

.L2:
		cmpl	$8, %eax			/* if */
		jg		.end
		movl	j, %ecx				/* %ecx = j */
		movl	%eax, %ecx			/* j = i */
		movl	%ecx, jg			/* j = %ecx */
		jmp		.L3

.L3:
		leal	1(%ecx), %esi		/* j + 1 */
		movl	arr(,%ecx,4), %edx	/* %edx = arr[j] */
		movl	arr(,%esi,4), %edi	/* %edi = arr[j+1] */
		cmpl	%edi, %edx			/* if (arr[j] > arr[j+1] )*/
		jl		.L4					/* If %edx is less than %edi*/
		movl	tmp, %ebx			/* %ebx = tmp */
		movl	%edx, %ebx			/* tmp = arr[j] */
		movl	%edi, arr(,%ecx,4)	/* arr[j] = arr[j+1] */
		movl	%ebx, arr(,%esi,4)	/* arr[j+1] = tmp */
		decl	%ecx				/* j-- */
		movl	%ecs, j				/* j = %ecx */		
		cmpl	$0, %ecx			/* Compare j and 0 */
		jl		.L4					/* If %ecx is less than 0 */
		jmp		.L3					/* If 0 is less than %ecx */ 

.L4:
		incl	%eax				/* i++ */
		movl	%eax, i				/* i = %eax */
		jmp		.L2

.end:
		popl	%ebp				/* Stack Frame (Epilog) */
		ret

		.size	main, .-main

.globl	arr
		.data
		.align	4
		.type	arr, @object
		.size	arr, 40

arr:
		.long	9
		.long	10
		.long	5
		.long	8
		.long	7
		.long	6
		.long	4
		.long	3
		.long	2
		.long	1

		.comm	tmp, 4, 4
		.comm	i, 4, 4
		.comm	j, 4, 4
		.ident	"GCC: (Debian 4.4.5-8) 4.4.5"
		.section	.note.GNU-stack,"",@progbits
