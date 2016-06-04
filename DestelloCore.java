
public class DestelloCore {
 private long[] reg= new long[18];
 /*//r0-r13 general purpose, 
  * r14- stack pointer,
  * r15- return address,
  * r16- program counter and
  * r17- psw(flags register) 
  * since flags.e and flags.gt cannot be set simultaneously therefore r[17]=1(flags.e) r[17]=2 flags.gt r[17]=0 default
  * */
 public int instMemSize;
 public int dataMemSize;
 private long inst;
 private int Rd;
 private  long ldResult;
 private long operand1;
 private long operand2;
 private long AluResult;
 private long branchPC;
 private long Off;
 
 Memory instmemory = new Memory(instMemSize);// instruction memory created 4K bytes
 Memory datamemory = new Memory(dataMemSize);// data Memory created 2K Bytes

 private int[] controlsignals = new int[24];// controlsignals[22]=isBranchTaken controlsignals[23]=nop
 public long time;
 
 private void fetch(){//start of fetch method
     if(controlsignals[22]==1)
	  {
		  reg[16]= branchPC;
	  }
     else
     {
    	 reg[(int) 16]= (long)(reg[16]+4);     
     }
     long PC = reg[16];
     
     inst= instmemory.readMemory(PC);
    
	  }//end of fetch
	 
 private void decode(){//start of decode
	 int opcode=(int) (inst>>27);            
	 int op =opcode&31;                     // opcode extracted
	 int i= (int)inst>>26;
	 i=i&1;                                 // immediate extracted
	  Rd= (int)inst>>22;
	  Rd=Rd&15;                             // destination address extracted 
	 int Rs1=(int)inst>>18;
	 Rs1=Rs1&15;                             //source 1 address extracted
	 int Rs2=(int) inst>>14;
	 Rs2=Rs2&15;                             //source 2 address extracted
	 int Imm=(int) (inst&65535);            // 16bit immediate extracted 
	 Off= inst&134217727;         //27 bit offset extracted
	 int modifiers=(int)inst>>16;           //2 bit modifiers extracted
	 modifiers=modifiers&3;
	 
	 long immx;                       // immediate value to be used for operations 
	 if(modifiers==1)
	 {
		 immx = Imm;
	 }
	 else if(modifiers==2)
	 {
		 immx=Imm<<16;
	 }
	 else
	 {
		 immx=Imm;
	 }
	 // fetch  operands for use in execution state
	
	 operand1= reg[Rs1];
    
	 if(i==1)
	 {
		 operand2= immx;
	 }
	 else
	 {
		 operand2=reg[Rs2];
	 }
// generation of control signals based upon opcode	 
	 switch(op)
	 {//start of switch case
	 case 0: //add isAdd
	 {
		 controlsignals[9]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 1://sub isSub
	 {
		 controlsignals[10]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 } 
	 case 2://mul isMul
	 {
		 controlsignals[12]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 } 	        
	 case 3://div isDiv
	 {
		 controlsignals[13]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 } 	 
	 case 4: // mod isMod
	 {
		 controlsignals[14]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 5: // cmp isCmp
	 {
		 controlsignals[11]=1;
		 
		 break;
	 }
	 case 6: // and isAnd
	 {
		 controlsignals[19]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 7: // or isOr
	 {
		 controlsignals[18]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 8: //not isNot
	 {
		 controlsignals[20]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 9: //mov isMov
	 {
		 controlsignals[21]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 10: //lsl isLsl
	 {
		 controlsignals[15]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 11: // lsr isLsr
	 {
		 controlsignals[16]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 12: // asr isAsr
	 {
		 controlsignals[17]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 13: //nop does nothing and breaks
	 {
		 break;
	 }
	 case 14: //ld isLd
	 {
		 controlsignals[1]=1;
		 controlsignals[6]=1;//is wb
		 break;
	 }
	 case 15: // st isSt
	 {
		 controlsignals[0]=1;
		 break;
	 }
	 case 16: //beq isBeq
	 {
		 controlsignals[2]=1;
		 break;
	 }
	 case 17: //bgt isBgt
	 {
		 controlsignals[3]=1;
		 break;
	 }
	 case 18: //b isUBranch
	 {
		 controlsignals[7]=1;
		 break;
	 }
	 case 19: //call isUBrranch
	 {
		 controlsignals[8]=1;//isCall
		 controlsignals[7]=1;//is UBranch
		 controlsignals[6]=1;//iswb
		 break;
	 }
	 case 20: //ret isUBranch
	 {
		 controlsignals[7]=1;
		 break;
	 }
	 case 30: //halt
	 {
		 controlsignals[23]=1;
		 break;
	 }
	 }//end of switch
// all the control signals are generated and decoding ends here	
	  
	 
	 
 }// end of decode
 
 // execute stage
 
 private void execute(){
	 aluUnit();//execution of all arithmetic and logical instructions
	 branchUnit();//execution of all branch instructions
	if(controlsignals[21]==1)// execution of mov
	 {
		 reg[Rd]=operand2;
	 }
	 
 }// end of execute
 //memory access and write back stages are not exclusively programmed they are amalgamated with execute stage only 

 private long aluUnit(){
	 
	 if(controlsignals[9]==1)// add
	 {
		 AluResult=operand1+operand2;
	 }
	 else if(controlsignals[10]==1)//sub
	 {
		 AluResult=operand1-operand2;
		 
	 }
	 else if(controlsignals[11]==1)//cmp
	 {
		 AluResult=operand1-operand2;
		 if(AluResult==0)
		 {
			reg[17]=1; 
		 }
		 else if (AluResult>0)
		 {
			 reg[17]=2;
		 }
		 else
		 {
			 reg[17]=0;
		 }
	 }
	 else if(controlsignals[12]==1)//mul
	 {
		 AluResult=operand1*operand2;
		
	 }
	 else if(controlsignals[13]==1)//div
	 {
		 AluResult=operand1/operand2;
		
	 }
	 else if(controlsignals[14]==1)//mod
	 {
		 AluResult=operand1%operand2;
		 
	 }
	 else if(controlsignals[15]==1)//lsl
	 {
		 AluResult=operand1<<operand2;
		 
	 }
	 else if(controlsignals[16]==1)//lsr
	 {
		 AluResult=operand1>>>operand2;
		 
	 }
	 else if(controlsignals[17]==1)//asr
	 {
		 AluResult=operand1>>operand2;
		 
	 }
	 else if(controlsignals[18]==1)//or
	 {
		 AluResult=operand1|operand2;
		
	 }
	 else if(controlsignals[19]==1)//and
	 {
		 AluResult=operand1&operand2;
		 
	 }
	 else if(controlsignals[20]==1)//not
	 {
		 AluResult=~operand1;
		 
	 }
	 return AluResult;
 }
 
 private void branchUnit(){
	if(controlsignals[2]==1)// beq 
	 {
		 if (reg[17]==1)
		 {
			 controlsignals[22]=1;
			 branchPC=Off<<2;
		 }
	 }
	 else if(controlsignals[3]==1)// bgt
	 {
		 if (reg[17]==2)
		 {
			 controlsignals[22]=1;
			 branchPC=Off<<2;
		 }		 
	 }
	 else if(controlsignals[4]==1)// ret
	 {
		 controlsignals[22]=1;
		 branchPC=reg[15];
	 }
	 else if(controlsignals[7]==1)//b,call,ret
	 {
		 controlsignals[22]=1;
	 }
	 else if(controlsignals[8]==1)
	 {
		 reg[15]= reg[16]+4;
	 }
 }
 
 void memoryAccessUnit(){
	 long mar;
	 long mdr;	 
	 if(controlsignals[0]==1)      //execution of store
	 {
	
		 mar = operand2 + operand1;
		 mdr = reg[Rd];
		 datamemory.writeMemory(mdr, mar);
	 }
	 else if(controlsignals[1]==1)  // execution of load
	 {
	    mar= operand1 +operand2;
	   ldResult =datamemory.readMemory(mar);
	 }
 }
 
 public void writeBackUnit(){
 	 
	 if(controlsignals[6]==1)//isWb is high
	 {
		 if(controlsignals[1]==1)//isLd is high
		  {
			 reg[Rd]=ldResult;
		  }
		 else if(controlsignals[9]==1)//isCall is high
		 {
			 reg[15]=reg[16]+4;
		 }
		 else
		 {
			 reg[Rd]=AluResult;
		 }
	 }
 }
 
 public void run( long pc )
 {
	 reg[16]=pc;
	 if(controlsignals[23]==0)// next instruction is processed only if halt is not encountered 
	 {
	 fetch();
	 decode();
	 execute();
	 memoryAccessUnit();
	 writeBackUnit();
	 } 
 }
 
 public void reset(){
	 int j;
	 for(j=0;j<18;j++)
	 {
		 reg[j]=0;
	 }
	 for(j=0;j<24;j++)
	 {
		 controlsignals[j]=0;
	 }
	   instMemSize=0;
	   dataMemSize=0;
	   inst=0;
	   Rd=0;
	   ldResult=0;
	   operand1=0;
	   operand2=0;
	   AluResult=0;
	   branchPC=0;
	   Off=0;
	 
 }
 }// end of class 
 

