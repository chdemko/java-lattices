package lattice;

/*
 * IS.java
 *
 * last update on May 2013
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import dgraph.DGraph;
import dgraph.Edge;
import dgraph.Node;
/**
* This class gives a representation for an implicational system (IS), i.e. a set of rules.
 *
 * An IS is composed of a treeset of comparable elements, and a treeset of rules
 * defined by class `Rule`.
 *
 * This class provides methods implementing classical transformation of an implicational system:
 * make proper, make minimum, make right maximal, make left minimal, make unary,
 * make canonical basis, make canonical direct basis and reduction.
 *
 * An implicational system owns properties of a closure system, and thus extends the abstract class
 * `ClosureSystem` and implements methods `getS` and `closure`.
 * Therefore, the closed set lattice of an IS can be generated by invoking method `closedSetLattice` of a closure system.
 *
 * An implicational system can be instancied from and save to a text file in the following format:
 * A list of elements separated by a space in the first line ;
 * then, each rule on a line, written like [premise] -> [conclusion]
 * where elements are separated by a space.
 *
 *     a b c d e
 *     a b -> c d
 *     c d -> e
 *
 * Copyright: 2013 University of La Rochelle, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of lattice, free package. You can redistribute it and/or modify
 * it under the terms of CeCILL-B license.
 *
 * @author Karell Bertet
 * @version 2013
 */
public class IS extends ClosureSystem
{
	/*--------------- FIELDS -----------------*/
	
	/** For the implicational rules of this component*/
	private TreeSet<Rule> sigma;
	/** For the elements space of this component */
	private TreeSet<Comparable> S;
	
	/* --------------- CONSTRUCTORS -----------*/
	
	/** Constructs a new empty component */
	public IS() { 
		this.sigma = new TreeSet<Rule>();
		this.S = new TreeSet<Comparable>();
	}	
		
	/** Constructs this component from the specified set of rules `sigma`.
	* @param sigma the set of rules 
	*/
	public IS (Collection<Rule> sigma) {
		this.sigma = new TreeSet<Rule>(sigma);
		this.S = new TreeSet<Comparable>();
		for (Rule r: this.sigma) {
			S.addAll(r.getPremise());
			S.addAll(r.getConclusion());
		}
	 }
		
	/** Constructs this component as a copy of the specified IS `s`.
	* Only structures (conataining reference of indexed elements) are copied.
	* @param s the IS to be copied 
	*/	
	public IS(IS s) {
		this.sigma = new TreeSet<Rule>(s.getRules());
		this.S = new TreeSet<Comparable>(s.getSet());
	}	
	/** Constructs this component from the specified file.
	*	
	* The file have to respect a certain format :
	*
     * An implicational system can be instancied from and save to a text file in the following format:
     * A list of elements separated by a space in the first line ;
     * then, each rule on a line, written like [premise] -> [conclusion]
     * where elements are separated by a space.
     *
     *     a b c d e
     *     a b -> c d
     *     c d -> e
     *
     * Each element must be declared on the first line, otherwise, it is not added in the rule
	* Each rule must have a non empty concusion, otherwise, it is not added in the component
	* @param filename the name of the file
	*/
	public IS (String filename) {
		try {
			this.sigma = new TreeSet<Rule>(); 
			this.S = new TreeSet<Comparable>();
			BufferedReader fichier = new BufferedReader(new FileReader(filename));						
			// first line : All elements of S separated by a space
			// a StringTokenizer is used to divide the line into different token,
            // considering spaces as separator.
			StringTokenizer st =  new StringTokenizer(fichier.readLine());	
			while (st.hasMoreTokens()) {
                String n = new String (st.nextToken());			
				this.addElement(n);
				}  
			// next lines : [elements of the premise separated by a space] -> [elements of the conclusion separated by a space]
			// a StringTokenizer is used to divide each rule. 
			String line;
			while ((line = fichier.readLine())!=null && !line.isEmpty()) {
				st = new StringTokenizer(line);
				Rule r = new Rule();
				boolean prem = true;
				while (st.hasMoreTokens()) {
					String word = st.nextToken();
					if (word.equals("->")) prem = false;
					else {
                        String x = null;						
						// search of x in S 
						for (Comparable e : this.S)
                            if (((String)e).equals(word)) x = (String)e;							
						if (x!=null) 
							if (prem) r.addToPremise(x);
							else r.addToConclusion(x);	
					}
				}
				if (!r.getConclusion().isEmpty()) this.addRule(r); }
		fichier.close(); }
		catch (Exception e) { e.printStackTrace(); }
	}

	/* ------------- ACCESSORS METHODS ------------------ */
  /** Generates a random IS with a specified number of nodes and rules
	 @param nbS the number of nodes of the generated IS
     @param nbR the number of rules of the generated IS
	  **/
    public static IS randomIS(int nbS, int nbR) {
	IS Sigma = new IS();
	// addition of elements
	for (int i=1; i<nbS; i++) {
		Sigma.addElement(new Integer(i));
		}
	// addition of rules
	for (int i=1; i<nbR; i++) {            
            ComparableSet conclusion = new ComparableSet();
            int choice = (int) Math.rint (nbS*Math.random());
            int j=1;
            for (Comparable c : Sigma.getSet()) {                
                if (j==choice)
                    conclusion.add(c);
                j++;
            }
            ComparableSet premisse = new ComparableSet();
            for (Comparable c : Sigma.getSet()) {
                choice = (int) Math.rint (nbS*Math.random());                
                if (choice < nbS/5)
                    premisse.add(c);                
            }
            Sigma.addRule(new Rule(premisse,conclusion));
		}
		return Sigma;
 	   }

	/* ------------- ACCESSORS METHODS ------------------ */
	
	/** Returns the set of rules 	*/	
	public TreeSet<Rule> getRules() { return this.sigma; }		
	/** Returns the set of indexed elements 	*/	
	public TreeSet<Comparable> getSet() { return this.S; }
	/** Returns the number of elements in the set S of this component	*/		
	public int nbElements() { return this.S.size(); }
	/** Returns the number of rules of this component	*/		
	public int nbRules() { return this.sigma.size(); }
	
	/* ------------- MODIFICATION METHODS ------------------ */	
		
	/** Adds the specified element to the set `S` of this component 
	* @param  e the comparable to be added
	* @return true if the element has been added to `S`
	*/
	public boolean addElement(Comparable e) {
		return S.add(e);
	}
	/** Adds the specified element to the set `S` of this component 
	* @param  X the treeset of comparable to be added
	* @return true if the element has been added to `S`
	*/
	public boolean addAllElements(TreeSet<Comparable> X) {
		boolean all = true;
		for (Comparable e : X)
			if (!S.add(e)) 
				all=false;
		return all;	
	}
	 
	/** Delete the specified element from the set `S` of this component
	* and from all the rule containing it.
	* @param e the comparable to be added
	* @return true if the element has been added to `S`
	*/
	public boolean deleteElement(Comparable e) {
		if (S.contains(e)) {
			S.remove(e);
			IS sauv = new IS(this);
			for (Rule r: sauv.sigma) {
				Rule new_r = new Rule(r.getPremise(),r.getConclusion());
				new_r.removeFromPremise(e);
				new_r.removeFromConclusion(e);
				if (!r.equals(new_r)) {
                    if (new_r.getConclusion().size()!=0)
                        this.replaceRule(r,new_r);
                    else
                        this.removeRule(r);
				}
            }
			return true;	
			}
		return false;
	} 
	
	/** Checks if the set S of this component contains the elements of the specified rule 
	* @param r the rule to be checked 
	* @return true if `S` contains all the elements of the rule 
	*/
	public boolean checkRuleElements(Rule r)	{
		for (Object e : r.getPremise()) if (!S.contains(e)) return false;
		for (Object e : r.getConclusion()) if (!S.contains(e)) return false;
		return true;
	}
	
	/** Checks if this component already contains the specified rule
	*
	* Rules are compared according to their premise and conclusion 
	* @param r the rule to be tested
	* @return true if `sigma` contains the rule
	*/
	public boolean containsRule(Rule r) {        
        return this.sigma.contains(r);
	}
	
	/** Adds the specified rule to this component 
	* @param r the rule to be added
	* @return true the rule has been added to if `sigma`
	*/	
	public boolean addRule(Rule r) {
		if (!this.containsRule(r) && this.checkRuleElements(r)) 
			return this.sigma.add(r);
		return false;
	}
	
	/** Removes the specified rule from the set of rules of this component
	* @param r the rule to be removed
	* @return true if the rule has been removed 
	*/	
	public boolean removeRule(Rule r) {
    	return this.sigma.remove(r);
	}

	/** Replaces the first specified rule by the second one
	* @param r1 the rule to be replaced by `r2`
	* @return true the rule has been replaced
	*/	
	public boolean replaceRule(Rule r1, Rule r2) {
		return (this.removeRule(r1) && this.addRule(r2));
	}

	/*-----------  SAVING METHODS -------------------- */

	/** Returns a string representation of this component.
     * The following format is used:
     *
	 * An implicational system can be instancied from and save to a text file in the following format:
     * A list of elements separated by a space in the first line ;
     * then, each rule on a line, written like [premise] -> [conclusion]
     * where elements are separated by a space.
     *
     *     a b c d e
     *     a b -> c d
     *     c d -> e
     *
    */
    public String toString()	{
        StringBuffer s = new StringBuffer();
        // first line : All elements of S separated by a space
		// a StringTokenizer is used to delete spaces in the
        // string description of each element of S
	for (Comparable e : this.S) {
            StringTokenizer st = new StringTokenizer(e.toString());
            while (st.hasMoreTokens())
                s.append(st.nextToken());
		s.append(" ");
            }
            s.append("\n");
        // next lines : a rule on each line, described by:
        // [elements of the premise separated by a space] -> [elements of the conclusion separated by a space]
	for (Rule r : this.sigma)
            s.append(r.toString()).append("\n");
	return s.toString();
	}
	
	/** Saves this component in a file which name is specified.
     * The following format is used:
     *
     * An implicational system can be instancied from and save to a text file in the following format:
     * A list of elements separated by a space in the first line ;
     * then, each rule on a line, written like [premise] -> [conclusion]
     * where elements are separated by a space.
     *
     *     a b c d e
     *     a b -> c d
     *     c d -> e
     *
	* @param filename the name of the file 
	*/
	public void toFile(String filename)	{
		try 	{
			BufferedWriter fichier = new BufferedWriter(new FileWriter(filename));
			fichier.write(this.toString());
            fichier.close();
    	}
    	catch (Exception e) { e.printStackTrace(); } 
	}

	/*-----------  PROPERTIES TEST METHODS -------------------- */
		
	/** Returns true if this component is a proper IS.
	*
	* This test is perfomed in O(|Sigma||S|) by testing conclusion of each rule
	*/
	public boolean isProper () {
		for (Rule r : this.sigma) 
			if (r.getPremise().contains(r.getConclusion())) 
				return false;
		return true;
	}

	/** Returns true if this component is an unary IS.
	*
	* This test is perfomed in O(|Sigma||S|) by testing conclusion of each rule
	*/	
	public boolean isUnary () {
		for (Rule r : this.sigma )
			if (r.getConclusion().size() > 1 )
				return false;
		return true;
	}

	/** Returns true if this component is a compact IS.
	*
	* This test is perfomed in O(|Sigma|^2|S|) by testing premises of each pair of rules
	*/	
	public boolean isCompact () {
	 	for(Rule r1: this.sigma)
		    for(Rule r2: this.sigma) 
			 	if (!r1.equals(r2) && (r1.getPremise().equals(r2.getPremise()))) 
					return false;
		return true;			
	}	


	/** Returns true if conclusion of rules of this component are closed.
	*
	* This test is perfomed in O(|Sigma||S|) by testing conclusion of each rule
	*/	
		public boolean isRightMaximal () {
		for (Rule r : this.sigma )
			if (!r.getConclusion().equals (this.closure(r.getConclusion())))
				return false;
		return true;
	}

	/** Returns true if this component is left minimal.
	*
	* This test is perfomed in O(|Sigma|^2|S|) by testing conclusions of each pair of rules
	*/	
	public boolean isLeftMinimal () {
		for (Rule r1 : this.sigma )
			for (Rule r2 : this.sigma )
			if (!r1.equals(r2) && r1.getPremise().containsAll(r2.getPremise()) 
				&&r1.getConclusion().equals(r2.getConclusion()))
				return false;
		return true;
	}

	/** Returns true if this component is direct.
	* 
	* This test is perfomed in O(|Sigma|^2|S|) by testing if closure of 
	* the premisse of each conclusion can be obtained by only one iteration 
	* on the set of rules.
	*/	
	public boolean isDirect () {
		for (Rule r1 : this.sigma ) {
			TreeSet <Comparable> onePass = new TreeSet (r1.getPremise());
			for (Rule r2 : this.sigma) 
				if (r1.getPremise().containsAll(r2.getPremise()))
					onePass.addAll (r2.getConclusion());
			if (!onePass.equals(this.closure(r1.getPremise())))			
				return false;
			}
		return true;
	}

	/** Returns true if this component is minimum.
	* 
	* This test is perfomed in O(|Sigma|^2|S|) by testing if closure of 
	* the premisse of each conclusion can be obtained by only one iteration 
	* on the set of rules.
	*/
	public boolean isMinimum () {
		IS tmp = new IS (this);
		tmp.makeRightMaximal();
		for (Rule r : sigma ) {
			 IS epsylon = new IS (tmp);
		     epsylon.removeRule(r);
			 TreeSet<Comparable> cl_this=this.closure(r.getPremise());
			 TreeSet<Comparable> cl_epsilon=epsylon.closure(r.getPremise());
			 if (cl_this.equals(cl_epsilon))	
			 	return false;
			}
		return true;
	}

	/** Returns true if this component is equal to its canonical direct basis.
	*
	* The canonical direct basis is computed before to be compare with this component.
	*
	* This test is performed in O(d|S|), where d corresponds to the number of rules 
	* that have to be added by the direct treatment. This number is exponential in the worst case.
	*/	
	public boolean isCanonicalDirectBasis () {
		IS CDB = new IS (this);
		CDB.makeCanonicalDirectBasis();
		if (this.isIncludedIn(CDB) && CDB.isIncludedIn(this))
			return true;
		else return false;	
	}
	
	/** Returns true if this component is equal to its canonical basis.
	*
	* The canonical basis is computed before to be compare with this component.
	*	
	* This treatment is performed in (|Sigma||S|cl) where O(cl) is the 
	* computation of a closure.
	*/	
	public boolean isCanonicalBasis () {
		IS CB = new IS (this);
		CB.makeCanonicalBasis();
		if (this.isIncludedIn(CB) && CB.isIncludedIn(this))
			return true;
		else return false;	
	}

	/** Compares by inclusion of the proper and unary form of this component 
	* with the specified one.
	*
	* @param is another IS 
	* @return true the rule has been replaced
	*/	
	public boolean isIncludedIn(IS is) {
		IS tmp = new IS (this);
		tmp.makeProper();
		tmp.makeUnary();
		is.makeProper();
		is.makeUnary();
		for (Rule r : tmp.sigma) 
			if (!is.containsRule(r))
				return false;
		return true;	
	}




	/*-----------  PROPERTIES MODIFICATION METHODS -------------------- */	
	
	/** Makes this component a proper IS.
	*
	* Elements that are at once in the conclusion and in the premise are deleted from the conclusion. 
	* When the obtained conclusion is an empty set, the rule is deleted from this component
	*
	* This treatment is performed in O(|Sigma||S|).
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/
	public int makeProper ()	{
		IS sauv = new IS(this);			
		for (Rule r : sauv.sigma) {
			// deletes elements of conclusion which are in the premise
			Rule new_r = new Rule (r.getPremise(), r.getConclusion());			
			for (Object e : r.getConclusion())
				if (new_r.getPremise().contains(e)) 
					new_r.removeFromConclusion (e);
			// replace the rule by the new rule is it has been modified
			if (!r.equals(new_r)) this.replaceRule(r,new_r);
			// delete rule with an empty conclusion
			if (new_r.getConclusion().isEmpty())	
				this.removeRule(new_r);
			}
		return sauv.nbRules()-this.nbRules();
		}	
		
	/** Makes this component an unary IS.
	*
	* This treatment is performed in O(|Sigma||S|)
	*
	* A rule with a non singleton as conclusion is replaced with a sets of rule, one rule for each 
	* element of the conclusion.
	*
	* This treatment is performed in O(|Sigma||S|).	
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/
	public int makeUnary () {
		IS sauv = new IS(this);		
		for (Rule r : sauv.sigma) {
			if (r.getConclusion().size()>1){                
				this.removeRule(r);
                TreeSet<Comparable> conclusion = r.getConclusion();
				TreeSet<Comparable> premise = r.getPremise();
				for (Comparable e : conclusion) {
					TreeSet<Comparable> new_c = new TreeSet();
					new_c.add(e);
					Rule nr = new Rule (premise, new_c);
					this.addRule(nr);                
					}
				}
			}
		return sauv.nbRules()-this.nbRules();
		}
		
	/** Replaces rules of same premise by only one rule.
	*
	* This treatment is performed in O(|sigma|^2|S|)
	*
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/
    public int makeCompact(){        
		IS sauv = new IS(this);
        for(Rule r1: sauv.sigma){
            if (this.containsRule(r1)) {
		 	ComparableSet new_conc = new ComparableSet();            
		    for(Rule r2: sauv.sigma) {                
			 	if (this.containsRule(r2) && !r1.equals(r2) && (r1.getPremise().equals(r2.getPremise()))) {
					new_conc.addAll(r2.getConclusion());                    
					boolean res = this.sigma.remove(r2);
					}
				}
			 if (new_conc.size()>0) {
				new_conc.addAll(r1.getConclusion());
				Rule new_r = new Rule (r1.getPremise(),new_conc);
                if (!r1.equals(new_r))
				this.replaceRule(r1,new_r);                
	          }                            
           }
	    }
		return sauv.nbRules()-this.nbRules();							
    	}

	/** Replaces conclusion of each rule with their closure without the premise.
	*
	* This treatment is performed in O(|sigma||S|cl), where O(cl) is the 
	* computation of a closure.
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/
    public int makeRightMaximal(){    
		int s = this.nbRules();
		this.makeCompact();
		IS sauv = new IS(this);	
		for(Rule r: sauv.sigma){									
           Rule new_r= new Rule(r.getPremise(), this.closure(r.getPremise()));
			  new_r.removeAllFromConclusion(new_r.getPremise());
			  if (!r.equals(new_r)) 
			  		this.replaceRule(r, new_r);
       }
		return s-this.nbRules();
    	}

	/** Makes this component a left minimal and compact IS.
	*
	* The unary form of this componant is first computed: if two rules have the 
	* same unary conclusion, the rule with the inclusion-maximal premise is deleted.
	*
	* Then, the left-minimal treatment is performed in O(|sigma|^2|S|))
	*	
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/
    public int makeLeftMinimal(){    
	 	this.makeUnary();
		IS sauv = new IS(this);		
    	for(Rule r1: sauv.sigma)
        for(Rule r2: sauv.sigma)
            if (!r1.equals(r2))
                    if( (r2.getPremise().containsAll(r1.getPremise()) )
						  &&(r1.getConclusion().equals(r2.getConclusion())) )
                        this.sigma.remove(r2);
		this.makeCompact();						
		return sauv.nbRules()-this.nbRules();
    	}	

	/** Makes this component a compact and direct IS.
	*
	* The unary and proper form of this componant is first computed. 
	* For two given rules r1 and r2, if the conclusion of r1 contains the premise of r1, 
	* then a new rule is addes, with r1.premisse + r2.premisse - r1.conclusion as premise, and 
	* r2.conclusion as conlusion. This treatment is performed in a recursive way until no new rule
	* is added.
	*
	* This treatment is performed in O(d|S|), where d  corresponds to the number of rules 
	* that have to be added by the direct treatment, that can be exponential in the worst case.
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/ 	
    public int makeDirect(){
	 	this.makeUnary();
		this.makeProper();
		int s = this.nbRules();
		boolean ok = true;
		while (ok) {
		IS sauv = new IS(this);		
 	     	for(Rule r1: sauv.sigma) 
                for(Rule r2: sauv.sigma)
                    if (!r1.equals(r2) && !r1.getPremise().containsAll(r2.getConclusion()))	{		
                    ComparableSet  C = new ComparableSet(r2.getPremise());
                    C.removeAll(r1.getConclusion());
                    C.addAll(r1.getPremise());
                    if (!C.containsAll(r2.getPremise())) {						
                        Rule new_r  = new Rule(C,r2.getConclusion());
                  /*new_r.addAllToPremise (r1.getPremise());
                  new_r.addAllToPremise (r2.getPremise());						
						new_r.removeAllFromPremise(r1.getConclusion());
						new_r.addAllToConclusion(r2.getConclusion() );*/
                  this.addRule(new_r);
						
                  }
					}

				if (this.nbRules() == sauv.nbRules())
					ok=false;
				}	
		this.makeCompact();							  
		return s-this.nbRules();
      }

	/** Makes this component a minimum and proper IS.
	*
	* A rule is deleted when the closure of its premisse remains the same 
	* even if this rule is suppressed.
	*
	* This treatment is performed in O(|sigma||S|cl) where O(cl) is the 
	* computation of a closure.
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/    
    public int makeMinimum(){	
		this.makeRightMaximal();
		IS sauv = new IS(this);				
      for(Rule r: sauv.sigma){
          IS epsylon= new IS(this);
          epsylon.removeRule(r);       
          if(epsylon.closure(r.getPremise()).equals(this.closure(r.getPremise()))) 
             this.removeRule(r);
      }                
		return sauv.nbRules()-this.nbRules();			 		
    }

	/** Replace this component by its canonical direct basis.
	*
	* The proper, unary and left minimal form of this component is first computed,
	* before to apply the recursive directe treatment, then the left minimal treatment.
	*
	* This treatment is performed in O(d), where d corresponds to the number of rules 
	* that have to be added by the direct treatment. This number is exponential in the worst case.
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/
    public int makeCanonicalDirectBasis(){
      int s = this.nbRules();
      this.makeProper();     
      this.makeLeftMinimal();
      this.makeDirect();
      this.makeLeftMinimal();
      this.makeCompact();
      return s-this.nbRules();
   }

	/** Replace this component by the canonical basis.
	*	
	* Conclusion of each rule is first replaced by its closure. 
	* Then, premise of each rule r is replaced by its closure in IS \ r.
	*
	* This treatment is performed in (|Sigma||S|cl) where O(cl) is the 
	* computation of a closure.
	* @return the difference between the number of rules of this component 
	* before and after this treatment
	*/
   public int makeCanonicalBasis(){
		this.makeMinimum();
		IS sauv = new IS(this);				
      for(Rule r: sauv.sigma){
         IS epsylon= new IS(this);
         epsylon.removeRule(r);
         Rule tmp= new Rule(epsylon.closure(r.getPremise()),r.getConclusion() );         
			if (!r.equals (tmp))
         this.replaceRule(r,tmp);
        }
		this.makeProper();
		return sauv.nbRules()-this.nbRules();
   }

   /* --------------- METHODS BASED ON GRAPH ------------ */

	/** Returns the representative graph of this component.
	*
	* Nodes of the graph are attributes of this components.
	* For each proper rule X+b->a, there is an {X}-valuated edge from a to b.
    * Notice that, for a rule b->a, the edge from a to b is valuated by emptyset.
     * and for the two rules X+b->a and Y+b->a, the edge from a to b is valuated by {X,Y}.
     * @return the representative graph of this component.
	*/
	public DGraph representativeGraph () {
        IS tmp = new IS (this);
        tmp.makeUnary();
		// nodes of the graph are elements not belonging to X
		DGraph pred = new DGraph();
        TreeMap<Comparable,Node> nodeCreated = new TreeMap<Comparable,Node>();
		for (Comparable x : tmp.getSet()) {
            Node n = new Node(x);
			pred.addNode (n);
            nodeCreated.put(x, n);
        }
		// an edge is added from b to a when there exists a rule X+a -> b or a -> b
		for (Rule r : tmp.getRules()) {
			for (Comparable a : r.getPremise()) {
                ComparableSet diff = new ComparableSet(r.getPremise());
                diff.remove(a);                
				Node from = nodeCreated.get(r.getConclusion().first());
				Node to = nodeCreated.get(a);
                Edge ed;
                if (pred.containsEdge(from, to))
                   ed = pred.edge(from,to);
                else {
                   ed = new Edge (from,to,new TreeSet<ComparableSet>());
                   pred.addEdge(ed);
                }
                ((TreeSet<ComparableSet>)ed.content()).add(diff);
				}
			}
		return pred;
	}
	/** Returns the dependance graph of this component.
	 *
     * Dependance graph of this component is the representative
     * graph of the canonical direct basis.
     * Therefore, the canonical direct basis has to be generated before
     * to compute its representativ graph, and
     * this treatment is performed in O(d), as for the canonical direct basis generation,
     * where d corresponds to the number of rules
	* that have to be added by the direct treatment. This number is exponential in the worst case.
     *
     * * @return the dependance graph of this component.
	*/
	public DGraph dependanceGraph () {
		IS BCD = new IS (this);
		BCD.makeCanonicalDirectBasis();
		BCD.makeUnary();
		return BCD.representativeGraph();
	}

    /** Removes from this component reducible elements.
    *
    * Reducible elements are elements equivalent by closure to others elements.   
    * They are computed by `getReducibleElements` of `ClosureSystem` 
    * in O(O(|Sigma||S|^2)
    * @return the set of reducibles removed elements, with their equivalent elements
    */
    public TreeMap <Comparable, TreeSet<Comparable>> reduction () {
        // compute the reducible elements            
        TreeMap Red = this.getReducibleElements();            
        // modify each rule
        for (Object x : Red.keySet()) {
            TreeSet <Rule> rules = this.getRules();
            rules = (TreeSet<Rule>) rules.clone();            
            for (Rule r : rules) {                
                Rule r2 = new Rule();
                boolean modif = false;
                // replace the reducible element by its equivalent in the premise
                TreeSet premise = r.getPremise();
                premise = (TreeSet) premise.clone();
                if (premise.contains(x)) {
                    premise.remove(x);
                    premise.addAll((TreeSet)Red.get(x));
                    r2.addAllToPremise(premise);
                    modif = true;
                } 
                else r2.addAllToPremise(premise);                
                // replace the reducible element by its equivalent in the conclusion
                TreeSet conclusion = r.getConclusion();
                conclusion = (TreeSet) conclusion.clone();
                if (conclusion.contains(x)) {
                    conclusion.remove(x);
                    conclusion.addAll((TreeSet)Red.get(x));
                    r2.addAllToConclusion(conclusion);
                    modif = true;
                }    
                else r2.addAllToConclusion(conclusion);                
                // replace the rule if modified
                if (modif)
                    this.replaceRule(r, r2);
            }
            // remove the reducible elements from the elements set
            this.deleteElement((Comparable)x);                        
        }
        return Red;
    }

	/* --------------- IMPLEMENTATION OF CLOSURESYSTEM ABSTRACT METHODS ------------ */

	/**  Builds the closure of a set X of indexed elements.
	*
	* The closure is initialized with X. The closure is incremented with 
	* the conclusion of each rule whose premise is included in it. 
	* Iterations over the rules are performed until no new elemnet has to be added 
	* in the closure.
	*
	* For direct IS, only one iteration is needed, and the treatment is 
	* performed in O(|Sigma||S|).
	*
	* For non direct IS, at most |S| iterations are needed, and this tratment
	* is performed in O(|Sigma||S|^2).
	* @param X a TreeSet of indexed elements
	* @return  the closure of X for this component 
	*/
	public TreeSet<Comparable> closure (TreeSet<Comparable> X) {
		TreeSet<Comparable> oldES = new TreeSet<Comparable>();
		// all the attributes are in their own closure
		TreeSet<Comparable> newES = new TreeSet<Comparable>(X);
        do {
			oldES.addAll(newES);		
			for (Rule r : this.sigma) {
				if (newES.containsAll(r.getPremise()))
					newES.addAll(r.getConclusion());
			}
		} while (!(oldES.equals(newES)));
		return newES;
	}
}	// end of IS
