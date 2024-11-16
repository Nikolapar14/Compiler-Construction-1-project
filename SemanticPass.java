package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;


public class SemanticPass extends VisitorAdaptor {
	
	int printCallCount = 0;
	int varDeclCount = 0;
	Obj currentMethod = null;
	boolean returnFound = false;
	boolean errorDetected = false;
	int nVars;
	
	
	
	Struct lastType = null;
	
	int numberConst = 0;
	boolean setNumberConst = false;
	
	char charConst = 'a';
	boolean setCharConst = false;

	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}

	public void visit(VarDecl varDecl) {
		varDeclCount++;
		if (Tab.currentScope.findSymbol(varDecl.getVarName()) == null) {
			report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);	    
			Tab.insert(Obj.Var, varDecl.getVarName(), lastType);   
		} else {
			report_error("Greska: ime " + varDecl.getVarName() + " jer vec deklarisano u istom opsegu!", varDecl);
		}
		
	}

	public void visit(StatementPrint print) {
		printCallCount++;
	}
	
	public void visit(ProgName progName) {
		
		progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
		Tab.openScope();
		
	}
	
	public void visit(Program program) {
		
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
		
	}
	
	public void visit(TypeAdvanced typeAdvanced) {
		
		Obj typeNode = Tab.find(typeAdvanced.getTypeName());
		if(typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + typeAdvanced.getTypeName() + " u tabeli simbola", null);
			typeAdvanced.struct = Tab.noType;
			lastType = Tab.noType;
		}else {
			
			if(Obj.Type == typeNode.getKind()) {
				
				typeAdvanced.struct = typeNode.getType();
				lastType = typeNode.getType();
				
			}else {
				report_error("Greska: Ime " + typeAdvanced.getTypeName() + " ne predstavlja tip", typeAdvanced);
				typeAdvanced.struct = Tab.noType;
				lastType = Tab.noType;
			}
			
		}
		
	}
	
	public void visit(SoloType soloType) {

		Obj typeNode = Tab.find(soloType.getTypeName());
		if (typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + soloType.getTypeName() + " u tabeli simbola", null);
			soloType.struct = Tab.noType;
			lastType = Tab.noType;
		} else {

			if (Obj.Type == typeNode.getKind()) {

				soloType.struct = typeNode.getType();
				lastType = typeNode.getType();

			} else {
				report_error("Greska: Ime " + soloType.getTypeName() + " ne predstavlja tip", soloType);
				soloType.struct = Tab.noType;
				lastType = Tab.noType;
			}

		}

	}
	
	public void visit(VoidType voidType) {
		
		voidType.struct = Tab.noType;
		lastType = Tab.noType;
		
	}
	
	public void visit(MethodTypeName methodTypeName) {
		
		
		
		currentMethod = Tab.insert(Obj.Meth,methodTypeName.getMethName(), lastType);
		methodTypeName.obj = currentMethod;
		Tab.openScope();
		report_info("Obradjuje se funkcija " + methodTypeName.getMethName(), methodTypeName);
	}
	
	
	
	public void visit(MethodDecl methodDecl) {
		
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		
		currentMethod = null;
		
	}
	
	public void visit(BasicDesignator designator) {
		
		Obj obj = Tab.find(designator.getName());
		if(obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine()+ " : ime "+designator.getName()+" nije deklarisano! ", null);
		}
		designator.obj = obj;
	}
	
	public void visit(ExtendedDesignator designator) {

		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName()
					+ " nije deklarisano! ", null);
		}
		designator.obj = obj;
	}
	
	public void visit(FactorDesignatorFunc funcCall) {
		
		Obj func = funcCall.getDesignator().obj;
		if(Obj.Meth == func.getKind()) {
			report_info("Pronadjen poziv funkcije " + func.getName() + " na liniji " + funcCall.getLine(), null);
			funcCall.struct = func.getType();
		}else {
			report_error("Greska na liniji " + funcCall.getLine()+" : ime " + func.getName() + " nije funkcija!", null);
			funcCall.struct = Tab.noType;
		}
		
	}
	
	public void visit(ConstDecl constDecl) {
		
		if (Tab.currentScope.findSymbol(constDecl.getConstName()) == null) {
			report_info("Deklarisana promenljiva " + constDecl.getConstName(), constDecl);	    
			Obj con = Tab.insert(Obj.Con, constDecl.getConstName(), lastType);
			
		} else {
			report_error("Greska: konstanta " + constDecl.getConstName() + " je vec deklarisana!", constDecl);
		}
		
	}
	
	public void visit(NumberConst numberCon) {
		
		numberConst = numberCon.getConstValue();
		this.setNumberConst = true;
		
	}
	
	
	
	
	
	
	
	
	
}
