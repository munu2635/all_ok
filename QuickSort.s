	.globl	Buf
	.data
	.align 32
	.type	Buf, @object
	.size	Buf, 40
Buf:
	.long	1
	.long	10
	.long	9
	.long	8
	.long	3
	.long	5
	.long	4
	.long	6
	.long	7
	.long	2
	.text
	.globl	Quick
	.type	Quick, @function
Quick:
.LFB5:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	subq	$32, %rsp
	movl	%edi, -20(%rbp)
	movl	%esi, -24(%rbp)
	movl	-24(%rbp), %eax
	cmpl	-20(%rbp), %eax
	jle	.L9
	movl	-24(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	leaq	Buf(%rip), %rax
	movl	(%rdx,%rax), %eax
	movl	%eax, -8(%rbp)
	movl	-20(%rbp), %eax
	subl	$1, %eax
	movl	%eax, -16(%rbp)
	movl	-24(%rbp), %eax
	movl	%eax, -12(%rbp)
	nop
.L3:
	addl	$1, -16(%rbp)
	movl	-16(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	leaq	Buf(%rip), %rax
	movl	(%rdx,%rax), %eax
	cmpl	%eax, -8(%rbp)
	jg	.L3
.L4:
	subl	$1, -12(%rbp)
	movl	-12(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	leaq	Buf(%rip), %rax
	movl	(%rdx,%rax), %eax
	cmpl	%eax, -8(%rbp)
	jl	.L4
	movl	-16(%rbp), %eax
	cmpl	-12(%rbp), %eax
	jge	.L10
	movl	-16(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	leaq	Buf(%rip), %rax
	movl	(%rdx,%rax), %eax
	movl	%eax, -4(%rbp)
	movl	-12(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	leaq	Buf(%rip), %rax
	movl	(%rdx,%rax), %edx
	movl	-16(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rcx
	leaq	Buf(%rip), %rax
	movl	%edx, (%rcx,%rax)
	movl	-12(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rcx
	leaq	Buf(%rip), %rax
	movl	-4(%rbp), %edx
	movl	%edx, (%rcx,%rax)
	jmp	.L3
.L10:
	nop
	movl	-16(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	leaq	Buf(%rip), %rax
	movl	(%rdx,%rax), %eax
	movl	%eax, -4(%rbp)
	movl	-24(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	leaq	Buf(%rip), %rax
	movl	(%rdx,%rax), %edx
	movl	-16(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rcx
	leaq	Buf(%rip), %rax
	movl	%edx, (%rcx,%rax)
	movl	-24(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rcx
	leaq	Buf(%rip), %rax
	movl	-4(%rbp), %edx
	movl	%edx, (%rcx,%rax)
	movl	-16(%rbp), %eax
	leal	-1(%rax), %edx
	movl	-20(%rbp), %eax
	movl	%edx, %esi
	movl	%eax, %edi
	call	Quick
	movl	-16(%rbp), %eax
	leal	1(%rax), %edx
	movl	-24(%rbp), %eax
	movl	%eax, %esi
	movl	%edx, %edi
	call	Quick
.L9:
	nop
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE5:
	.size	Quick, .-Quick
	.section	.rodata
.LC0:
	.string	"%4d "
	.text
	.globl	DisplayBuffer
	.type	DisplayBuffer, @function
DisplayBuffer:
.LFB6:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	subq	$16, %rsp
	movl	$0, -4(%rbp)
	jmp	.L12
.L14:
	movl	-4(%rbp), %ecx
	movl	$1717986919, %edx
	movl	%ecx, %eax
	imull	%edx
	sarl	$2, %edx
	movl	%ecx, %eax
	sarl	$31, %eax
	subl	%eax, %edx
	movl	%edx, %eax
	sall	$2, %eax
	addl	%edx, %eax
	addl	%eax, %eax
	subl	%eax, %ecx
	movl	%ecx, %edx
	testl	%edx, %edx
	jne	.L13
	movl	$10, %edi
	call	putchar@PLT
.L13:
	movl	-4(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	leaq	Buf(%rip), %rax
	movl	(%rdx,%rax), %eax
	movl	%eax, %esi
	leaq	.LC0(%rip), %rdi
	movl	$0, %eax
	call	printf@PLT
	addl	$1, -4(%rbp)
.L12:
	cmpl	$9, -4(%rbp)
	jle	.L14
	movl	$10, %edi
	call	putchar@PLT
	nop
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE6:
	.size	DisplayBuffer, .-DisplayBuffer
	.globl	main
	.type	main, @function
main:
.LFB7:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	movl	$9, %esi
	movl	$0, %edi
	call	Quick
	movl	$0, %eax
	call	DisplayBuffer
	movl	$10, %edi
	call	putchar@PLT
	nop
	popq	%rbp
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE7:
	.size	main, .-main

