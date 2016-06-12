package org.thegalactic.lattice;

/*
 * ImplicationalSystem.java
 *
 * Copyright: 2010-2015 Karell Bertet, France
 * Copyright: 2015-2016 The Galactic Organization, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices.
 * You can redistribute it and/or modify it under the terms of the CeCILL-B license.
 */
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.thegalactic.dgraph.DGraph;
import org.thegalactic.dgraph.Edge;
import org.thegalactic.dgraph.Node;
import org.thegalactic.io.Filer;
import org.thegalactic.lattice.io.ImplicationalSystemIOFactory;
import org.thegalactic.util.ComparableSet;

/**
 * This class gives a representation for an implicational system
 * (ImplicationalSystem), a set of rules.
 *
 * An ImplicationalSystem is composed of a TreeSet of comparable elements, and a
 * TreeSet of rules defined by class {@link Rule}.
 *
 * This class provides methods implementing classical transformation of an
 * implicational system : make proper, make minimum, make right maximal, make
 * left minimal, make unary, make canonical basis, make canonical direct basis
 * and reduction.
 *
 * An implicational system owns properties of a closure system, and thus extends
 * the abstract class {@link ClosureSystem} and implements methods
 * {@link #getSet} and {@link #closure}.
 *
 * Therefore, the closed set lattice of an ImplicationalSystem can be generated
 * by invoking method {@link #closedSetLattice} of a closure system.
 *
 * An implicational system can be instancied from and save to a text file in the
 * following format: - a list of elements separated by a space in the first line
 * ; - then, each rule on a line, written like [premise] -> [conclusion] where
 * elements are separated by a space.
 *
 * ~~~
 * a b c d e
 * a b -> c d
 * c d -> e
 * ~~~
 *
 * ![ImplicationalSystem](ImplicationalSystem.png)
 *
 * @uml ImplicationalSystem.png
 * !include resources/org/thegalactic/lattice/ImplicationalSystem.iuml
 * !include resources/org/thegalactic/lattice/ClosureSystem.iuml
 *
 * hide members
 * show ImplicationalSystem members
 * class ImplicationalSystem #LightCyan
 * title ImplicationalSystem UML graph
 */
public class ImplicationalSystem extends ClosureSystem {

    /*
     * --------------- FIELDS -----------------
     */
    /**
     * Generates a random ImplicationalSystem with a specified number of nodes
     * and rules.
     *
     * @param nbS the number of nodes of the generated ImplicationalSystem
     * @param nbR the number of rules of the generated ImplicationalSystem
     *
     * @return a random implicational system with a specified number of nodes
     * and rules.
     */
    public static ImplicationalSystem random(int nbS, int nbR) {
        ImplicationalSystem sigma = new ImplicationalSystem();
        // addition of elements
        for (int i = 0; i < nbS; i++) {
            sigma.addElement(new Integer(i));
        }
        // addition of rules
        //for (int i = 0; i < nbR; i++) { One could get twice the same rule ...
        while (sigma.getRules().size() < nbR) {
            ComparableSet conclusion = new ComparableSet();
            int choice = (int) Math.rint(nbS * Math.random());
            int j = 1;
            for (Comparable c : sigma.getSet()) {
                if (j == choice) {
                    conclusion.add(c);
                }
                j++;
            }
            ComparableSet premisse = new ComparableSet();
            for (Comparable c : sigma.getSet()) {
                choice = (int) Math.rint(nbS * Math.random());
                if (choice < nbS / 5) {
                    premisse.add(c);
                }
            }
            //if (!premisse.isEmpty()) {
            sigma.addRule(new Rule(premisse, conclusion));
            //}
        }
        return sigma;
    }

    /**
     * For the implicational rules of this component.
     */
    private TreeSet<Rule> sigma;

    /**
     * For the elements space of this component.
     */
    private TreeSet<Comparable> set;

    /*
     * --------------- CONSTRUCTORS -----------
     */
    /**
     * Constructs a new empty component.
     */
    public ImplicationalSystem() {
        this.init();
    }

    /**
     * Constructs this component from the specified set of rules `sigma`.
     *
     * @param sigma the set of rules.
     */
    public ImplicationalSystem(Collection<Rule> sigma) {
        this.sigma = new TreeSet<Rule>(sigma);
        this.set = new TreeSet<Comparable>();
        for (Rule rule : this.sigma) {
            set.addAll(rule.getPremise());
            set.addAll(rule.getConclusion());
        }
    }

    /**
     * Constructs this component as a copy of the specified ImplicationalSystem
     * `is`.
     *
     * Only structures (conataining reference of indexed elements) are copied.
     *
     * @param is the implicational system to be copied
     */
    public ImplicationalSystem(ImplicationalSystem is) {
        this.sigma = new TreeSet<Rule>(is.getRules());
        this.set = new TreeSet<Comparable>(is.getSet());
    }

    /**
     * Constructs this component from the specified file.
     *
     * The file have to respect a certain format:
     *
     * An implicational system can be instancied from and save to a text file in
     * the following format: - A list of elements separated by a space in the
     * first line ; - then, each rule on a line, written like [premise] ->
     * [conclusion] where elements are separated by a space.
     *
     * ~~~ a b c d e a b -> c d c d -> e ~~~
     *
     * Each element must be declared on the first line, otherwise, it is not
     * added in the rule Each rule must have a non empty concusion, otherwise,
     * it is not added in the component
     *
     * @param filename the name of the file
     *
     * @throws IOException When an IOException occurs
     */
    public ImplicationalSystem(String filename) throws IOException {
        this.parse(filename);
    }

    /**
     * Initialise the implicational system.
     *
     * @return this for chaining
     */
    public ImplicationalSystem init() {
        this.sigma = new TreeSet<Rule>();
        this.set = new TreeSet<Comparable>();
        return this;
    }

    /*
     * ------------- ACCESSORS METHODS ------------------
     */
    /**
     * Returns the set of rules.
     *
     * @return the set of rules of this component.
     */
    public SortedSet<Rule> getRules() {
        return Collections.unmodifiableSortedSet((SortedSet<Rule>) this.sigma);
    }

    /**
     * Returns the set of indexed elements.
     *
     * @return the elements space of this component.
     */
    public SortedSet<Comparable> getSet() {
        return Collections.unmodifiableSortedSet((SortedSet<Comparable>) this.set);
    }

    /**
     * Returns the number of elements in the set S of this component.
     *
     * @return the number of elements in the elements space of this component.
     */
    public int sizeElements() {
        return this.set.size();
    }

    /**
     * Returns the number of rules of this component.
     *
     * @return the number of rules of this component.
     */
    public int sizeRules() {
        return this.sigma.size();
    }

    /*
     * ------------- MODIFICATION METHODS ------------------
     */
    /**
     * Adds the specified element to the set `S` of this component.
     *
     * @param e the comparable to be added
     *
     * @return true if the element has been added to `S`
     */
    public boolean addElement(Comparable e) {
        return set.add(e);
    }

    /**
     * Adds the specified element to the set `S` of this component.
     *
     * @param x the treeset of comparable to be added
     *
     * @return true if the element has been added to `S`
     */
    public boolean addAllElements(TreeSet<Comparable> x) {
        boolean all = true;
        for (Comparable e : x) {
            if (!set.add(e)) {
                all = false;
            }
        }
        return all;
    }

    /**
     * Delete the specified element from the set `S` of this component and from
     * all the rule containing it.
     *
     * @param e the comparable to be added
     *
     * @return true if the element has been added to `S`
     */
    public boolean deleteElement(Comparable e) {
        if (set.contains(e)) {
            set.remove(e);
            ImplicationalSystem save = new ImplicationalSystem(this);
            for (Rule rule : save.sigma) {
                Rule newR = new Rule(rule.getPremise(), rule.getConclusion());
                newR.removeFromPremise(e);
                newR.removeFromConclusion(e);
                if (!rule.equals(newR)) {
                    if (newR.getConclusion().size() != 0) {
                        this.replaceRule(rule, newR);
                    } else {
                        this.removeRule(rule);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if the set S of this component contains the elements of the
     * specified rule.
     *
     * @param rule the rule to be checked
     *
     * @return true if `S` contains all the elements of the rule
     */
    public boolean checkRuleElements(Rule rule) {
        for (Object e : rule.getPremise()) {
            if (!set.contains(e)) {
                return false;
            }
        }
        for (Object e : rule.getConclusion()) {
            if (!set.contains(e)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this component already contains the specified rule.
     *
     * Rules are compared according to their premise and conclusion
     *
     * @param rule the rule to be tested
     *
     * @return true if `sigma` contains the rule
     */
    public boolean containsRule(Rule rule) {
        return this.sigma.contains(rule);
    }

    /**
     * Adds the specified rule to this component.
     *
     * @param rule the rule to be added
     *
     * @return true the rule has been added to if `sigma`
     */
    public boolean addRule(Rule rule) {
        if (!this.containsRule(rule) && this.checkRuleElements(rule)) {
            return this.sigma.add(rule);
        }
        return false;
    }

    /**
     * Removes the specified rule from the set of rules of this component.
     *
     * @param rule the rule to be removed
     *
     * @return true if the rule has been removed
     */
    public boolean removeRule(Rule rule) {
        return this.sigma.remove(rule);
    }

    /**
     * Replaces the first specified rule by the second one.
     *
     * @param rule1 the rule to be replaced by `rule2`
     * @param rule2 the replacing rule
     *
     * @return true the rule has been replaced
     */
    public boolean replaceRule(Rule rule1, Rule rule2) {
        return this.removeRule(rule1) && this.addRule(rule2);
    }

    /*
     * ----------- SAVING METHODS --------------------
     */
    /**
     * Returns a string representation of this component.
     *
     * The following format is used:
     *
     * An implicational system can be instancied from and save to a text file in
     * the following format: - A list of elements separated by a space in the
     * first line ; - then, each rule on a line, written like [premise] ->
     * [conclusion] where elements are separated by a space.
     *
     * ~~~ a b c d e a b -> c d c d -> e ~~~
     *
     * @return a string representation of this component.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        // first line : All elements of S separated by a space
        // a StringTokenizer is used to delete spaces in the
        // string description of each element of S
        for (Comparable e : this.set) {
            StringTokenizer st = new StringTokenizer(e.toString());
            while (st.hasMoreTokens()) {
                s.append(st.nextToken());
            }
            s.append(" ");
        }
        String newLine = System.getProperty("line.separator");
        s.append(newLine);
        // next lines : a rule on each line, described by:
        // [elements of the premise separated by a space] -> [elements of the conclusion separated by a space]
        for (Rule rule : this.sigma) {
            s.append(rule.toString()).append(newLine);
        }
        return s.toString();
    }

    /**
     * Save the description of this component in a file whose name is specified.
     *
     * @param filename the name of the file
     *
     * @throws IOException When an IOException occurs
     */
    public void save(final String filename) throws IOException {
        Filer.getInstance().save(this, ImplicationalSystemIOFactory.getInstance(), filename);
    }

    /**
     * Parse the description of this component from a file whose name is
     * specified.
     *
     * @param filename the name of the file
     *
     * @return this for chaining
     *
     * @throws IOException When an IOException occurs
     */
    public ImplicationalSystem parse(final String filename) throws IOException {
        this.init();
        Filer.getInstance().parse(this, ImplicationalSystemIOFactory.getInstance(), filename);
        return this;
    }

    /*
     * ----------- PROPERTIES TEST METHODS --------------------
     */
    /**
     * Returns true if this component is a proper ImplicationalSystem.
     *
     * This test is perfomed in O(|Sigma||S|) by testing conclusion of each rule
     *
     * @return true if and only if this component is a proper implicational
     * system.
     */
    public boolean isProper() {
        for (Rule rule : this.sigma) {
            for (Object c : rule.getConclusion()) {
                if (rule.getPremise().contains(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if this component is an unary ImplicationalSystem.
     *
     * This test is perfomed in O(|Sigma||S|) by testing conclusion of each rule
     *
     * @return true if this component is an unary ImplicationalSystem.
     */
    public boolean isUnary() {
        for (Rule rule : this.sigma) {
            if (rule.getConclusion().size() > 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this component is a compact ImplicationalSystem.
     *
     * This test is perfomed in O(|Sigma|^2|S|) by testing premises of each pair
     * of rules
     *
     * @return true if this component is a compact ImplicationalSystem.
     */
    public boolean isCompact() {
        for (Rule rule1 : this.sigma) {
            for (Rule rule2 : this.sigma) {
                if (!rule1.equals(rule2) && rule1.getPremise().equals(rule2.getPremise())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if conclusion of rules of this component are closed.
     *
     * This test is perfomed in O(|Sigma||S|) by testing conclusion of each rule
     *
     * @return true if conclusion of rules of this component are closed.
     */
    public boolean isRightMaximal() {
        for (Rule rule : this.sigma) {
            if (!rule.getConclusion().containsAll(this.closure(rule.getConclusion()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this component is left minimal.
     *
     * This test is perfomed in O(|Sigma|^2|S|) by testing conclusions of each
     * pair of rules
     *
     * @return true if this component is left minimal.
     */
    public boolean isLeftMinimal() {
        for (Rule rule1 : this.sigma) {
            for (Rule rule2 : this.sigma) {
                if (!rule1.equals(rule2)
                        && rule1.getPremise().containsAll(rule2.getPremise())
                        && rule1.getConclusion().equals(rule2.getConclusion())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if this component is direct.
     *
     * This test is perfomed in O(|Sigma|^2|S|) by testing if closure of the
     * premisse of each conclusion can be obtained by only one iteration on the
     * set of rules.
     *
     * @return true if this component is direct.
     */
    public boolean isDirect() {
        for (Rule rule1 : this.sigma) {
            TreeSet<Comparable> onePass = new TreeSet(rule1.getPremise());
            for (Rule rule2 : this.sigma) {
                if (rule1.getPremise().containsAll(rule2.getPremise())) {
                    onePass.addAll(rule2.getConclusion());
                }
            }
            if (!onePass.equals(this.closure(rule1.getPremise()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this component is minimum.
     *
     * This test is perfomed in O(|Sigma|^2|S|) by testing if closure of the
     * premisse of each conclusion can be obtained by only one iteration on the
     * set of rules.
     *
     * @return true if this component is minimum.
     */
    public boolean isMinimum() {
        ImplicationalSystem tmp = new ImplicationalSystem(this);
        tmp.makeRightMaximal();
        for (Rule rule : sigma) {
            ImplicationalSystem epsilon = new ImplicationalSystem(tmp);
            epsilon.removeRule(rule);
            TreeSet<Comparable> clThis = this.closure(rule.getPremise());
            TreeSet<Comparable> clEpsilon = epsilon.closure(rule.getPremise());
            if (clThis.equals(clEpsilon)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this component is equal to its canonical direct basis.
     *
     * The canonical direct basis is computed before to be compare with this
     * component.
     *
     * This test is performed in O(d|S|), where d corresponds to the number of
     * rules that have to be added by the direct treatment. This number is
     * exponential in the worst case.
     *
     * @return true if this component is equal to its canonical direct basis.
     */
    public boolean isCanonicalDirectBasis() {
        ImplicationalSystem cdb = new ImplicationalSystem(this);
        cdb.makeCanonicalDirectBasis();
        return this.isIncludedIn(cdb) && cdb.isIncludedIn(this);
    }

    /**
     * Returns true if this component is equal to its canonical basis.
     *
     * The canonical basis is computed before to be compare with this component.
     *
     * This treatment is performed in (|Sigma||S|cl) where O(cl) is the
     * computation of a closure.
     *
     * @return true if this component is equal to its canonical basis.
     */
    public boolean isCanonicalBasis() {
        ImplicationalSystem cb = new ImplicationalSystem(this);
        cb.makeCanonicalBasis();
        return this.isIncludedIn(cb) && cb.isIncludedIn(this);
    }

    /**
     * Compares by inclusion of the proper and unary form of this component with
     * the specified one.
     *
     * @param is another ImplicationalSystem
     *
     * @return true if really include in this componenet.
     */
    public boolean isIncludedIn(ImplicationalSystem is) {
        ImplicationalSystem tmp = new ImplicationalSystem(this);
        tmp.makeProper();
        tmp.makeUnary();
        is.makeProper();
        is.makeUnary();
        for (Rule rule : tmp.sigma) {
            if (!is.containsRule(rule)) {
                return false;
            }
        }
        return true;
    }

    /*
     * ----------- PROPERTIES MODIFICATION METHODS --------------------
     */
    /**
     * Makes this component a proper ImplicationalSystem.
     *
     * Elements that are at once in the conclusion and in the premise are
     * deleted from the conclusion. When the obtained conclusion is an empty
     * set, the rule is deleted from this component
     *
     * This treatment is performed in O(|Sigma||S|).
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeProper() {
        ImplicationalSystem save = new ImplicationalSystem(this);
        for (Rule rule : save.sigma) {
            // deletes elements of conclusion which are in the premise
            Rule newR = new Rule(rule.getPremise(), rule.getConclusion());
            for (Object e : rule.getConclusion()) {
                if (newR.getPremise().contains(e)) {
                    newR.removeFromConclusion(e);
                }
            }
            // replace the rule by the new rule is it has been modified
            if (!rule.equals(newR)) {
                this.replaceRule(rule, newR);
            }
            // delete rule with an empty conclusion
            if (newR.getConclusion().isEmpty()) {
                this.removeRule(newR);
            }
        }
        return save.sizeRules() - this.sizeRules();
    }

    /**
     * Makes this component an unary ImplicationalSystem.
     *
     * This treatment is performed in O(|Sigma||S|)
     *
     * A rule with a non singleton as conclusion is replaced with a sets of
     * rule, one rule for each element of the conclusion.
     *
     * This treatment is performed in O(|Sigma||S|).
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeUnary() {
        ImplicationalSystem save = new ImplicationalSystem(this);
        for (Rule rule : save.sigma) {
            if (rule.getConclusion().size() > 1) {
                this.removeRule(rule);
                TreeSet<Comparable> conclusion = rule.getConclusion();
                TreeSet<Comparable> premise = rule.getPremise();
                for (Comparable e : conclusion) {
                    TreeSet<Comparable> newC = new TreeSet();
                    newC.add(e);
                    Rule newRule = new Rule(premise, newC);
                    this.addRule(newRule);
                }
            }
        }
        return save.sizeRules() - this.sizeRules();
    }

    /**
     * Replaces rules of same premise by only one rule.
     *
     * This treatment is performed in O(|sigma|^2|S|)
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeCompact() {
        ImplicationalSystem save = new ImplicationalSystem(this);
        int before = this.sigma.size();
        this.sigma = new TreeSet();

        while (save.sigma.size() > 0) {
            Rule rule1 = save.sigma.first();
            ComparableSet newConc = new ComparableSet();
            Iterator<Rule> it2 = save.sigma.iterator();
            while (it2.hasNext()) {
                Rule rule2 = it2.next();
                if (!rule1.equals(rule2) && rule1.getPremise().equals(rule2.getPremise())) {
                    newConc.addAll(rule2.getConclusion());
                    it2.remove();
                }
            }
            if (newConc.size() > 0) {
                newConc.addAll(rule1.getConclusion());
                Rule newR = new Rule(rule1.getPremise(), newConc);
                this.addRule(newR);
            } else {
                this.addRule(rule1);
            }
            save.removeRule(rule1);
        }
        return before - this.sigma.size();
    }

    /**
     * Replaces association rules of same premise, same support and same
     * confidence by only one rule.
     *
     * This treatment is performed in O(|sigma|^2|S|)
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeCompactAssociation() {
        ImplicationalSystem save = new ImplicationalSystem(this);
        int before = this.sigma.size();
        this.sigma = new TreeSet();

        while (save.sigma.size() > 0) {
            AssociationRule rule1 = (AssociationRule) save.sigma.first();
            ComparableSet newConc = new ComparableSet();
            Iterator<Rule> it2 = save.sigma.iterator();
            while (it2.hasNext()) {
                AssociationRule rule2 = (AssociationRule) it2.next();
                if (!rule1.equals(rule2) && rule1.getPremise().equals(rule2.getPremise())
                        && rule1.getConfidence() == rule2.getConfidence() && rule1.getSupport() == rule2.getSupport()) {
                    newConc.addAll(rule2.getConclusion());
                    it2.remove();
                }
            }
            if (newConc.size() > 0) {
                newConc.addAll(rule1.getConclusion());
                AssociationRule newR = new AssociationRule(rule1.getPremise(), newConc, rule1.getSupport(), rule1.getConfidence());
                this.addRule(newR);
            } else {
                this.addRule(rule1);
            }
            save.removeRule(rule1);
        }
        return before - this.sigma.size();
    }

    /**
     * Replaces conclusion of each rule with their closure without the premise.
     *
     * This treatment is performed in O(|sigma||S|cl), where O(cl) is the
     * computation of a closure.
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeRightMaximal() {
        int s = this.sizeRules();
        this.makeCompact();
        ImplicationalSystem save = new ImplicationalSystem(this);
        for (Rule rule : save.sigma) {
            Rule newR = new Rule(rule.getPremise(), this.closure(rule.getPremise()));
            if (!rule.equals(newR)) {
                this.replaceRule(rule, newR);
            }
        }
        return s - this.sizeRules();
    }

    /**
     * Makes this component a left minimal and compact ImplicationalSystem.
     *
     * The unary form of this componant is first computed: if two rules have the
     * same unary conclusion, the rule with the inclusion-maximal premise is
     * deleted.
     *
     * Then, the left-minimal treatment is performed in O(|sigma|^2|S|))
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeLeftMinimal() {
        this.makeUnary();
        ImplicationalSystem save = new ImplicationalSystem(this);
        for (Rule rule1 : save.sigma) {
            for (Rule rule2 : save.sigma) {
                if (!rule1.equals(rule2)
                        && rule2.getPremise().containsAll(rule1.getPremise())
                        && rule1.getConclusion().equals(rule2.getConclusion())) {
                    this.sigma.remove(rule2);
                }
            }
        }
        this.makeCompact();
        return save.sizeRules() - this.sizeRules();
    }

    /**
     * Makes this component a compact and direct ImplicationalSystem.
     *
     * The unary and proper form of this componant is first computed. For two
     * given rules rule1 and rule2, if the conclusion of rule1 contains the
     * premise of rule1, then a new rule is addes, with rule1.premisse +
     * rule2.premisse - rule1.conclusion as premise, and rule2.conclusion as
     * conlusion. This treatment is performed in a recursive way until no new
     * rule is added.
     *
     * This treatment is performed in O(d|S|), where d corresponds to the number
     * of rules that have to be added by the direct treatment, that can be
     * exponential in the worst case.
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeDirect() {
        this.makeUnary();
        this.makeProper();
        int s = this.sizeRules();
        boolean ok = true;
        while (ok) {
            ImplicationalSystem save = new ImplicationalSystem(this);
            for (Rule rule1 : save.sigma) {
                for (Rule rule2 : save.sigma) {
                    if (!rule1.equals(rule2) && !rule1.getPremise().containsAll(rule2.getConclusion())) {
                        ComparableSet c = new ComparableSet(rule2.getPremise());
                        c.removeAll(rule1.getConclusion());
                        c.addAll(rule1.getPremise());
                        if (!c.containsAll(rule2.getPremise())) {
                            Rule newR = new Rule(c, rule2.getConclusion());
                            // new_rule.addAllToPremise (rule1.getPremise());
                            // new_rule.addAllToPremise (rule2.getPremise());
                            // new_rule.removeAllFromPremise(rule1.getConclusion());
                            // new_rule.addAllToConclusion(rule2.getConclusion() );
                            this.addRule(newR);
                        }
                    }
                }
            }
            if (this.sizeRules() == save.sizeRules()) {
                ok = false;
            }
        }
        this.makeCompact();
        return s - this.sizeRules();
    }

    /**
     * Makes this component a minimum and proper ImplicationalSystem.
     *
     * A rule is deleted when the closure of its premisse remains the same even
     * if this rule is suppressed.
     *
     * This treatment is performed in O(|sigma||S|cl) where O(cl) is the
     * computation of a closure.
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeMinimum() {
        this.makeRightMaximal();
        ImplicationalSystem save = new ImplicationalSystem(this);
        for (Rule rule : save.sigma) {
            ImplicationalSystem epsilon = new ImplicationalSystem(this);
            epsilon.removeRule(rule);
            if (epsilon.closure(rule.getPremise()).equals(this.closure(rule.getPremise()))) {
                this.removeRule(rule);
            }
        }
        return save.sizeRules() - this.sizeRules();
    }

    /**
     * Replace this component by its canonical direct basis.
     *
     * The proper, unary and left minimal form of this component is first
     * computed, before to apply the recursive directe treatment, then the left
     * minimal treatment.
     *
     * This treatment is performed in O(d), where d corresponds to the number of
     * rules that have to be added by the direct treatment. This number is
     * exponential in the worst case.
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeCanonicalDirectBasis() {
        int s = this.sizeRules();
        this.makeProper();
        this.makeLeftMinimal();
        this.makeDirect();
        this.makeLeftMinimal();
        this.makeCompact();
        return s - this.sizeRules();
    }

    /**
     * Replace this component by the canonical basis.
     *
     * Conclusion of each rule is first replaced by its closure. Then, premise
     * of each rule r is replaced by its closure in ImplicationalSystem \ rule.
     * This treatment is performed in (|Sigma||S|cl) where O(cl) is the
     * computation of a closure.
     *
     * @return the difference between the number of rules of this component
     * before and after this treatment
     */
    public int makeCanonicalBasis() {
        this.makeMinimum();
        ImplicationalSystem save = new ImplicationalSystem(this);
        for (Rule rule : save.sigma) {
            ImplicationalSystem epsilon = new ImplicationalSystem(this);
            epsilon.removeRule(rule);
            Rule tmp = new Rule(epsilon.closure(rule.getPremise()), rule.getConclusion());
            if (!rule.equals(tmp)) {
                this.replaceRule(rule, tmp);
            }
        }
        this.makeProper();
        return save.sizeRules() - this.sizeRules();
    }

    /*
     * --------------- METHODS BASED ON GRAPH ------------
     */
    /**
     * Returns the representative graph of this component.
     *
     * Nodes of the graph are attributes of this components. For each proper
     * rule X+b->a, there is an {X}-valuated edge from a to b. Notice that, for
     * a rule b->a, the edge from a to b is valuated by emptyset. and for the
     * two rules X+b->a and Y+b->a, the edge from a to b is valuated by {X,Y}.
     *
     * @return the representative graph of this component.
     */
    public DGraph representativeGraph() {
        ImplicationalSystem tmp = new ImplicationalSystem(this);
        tmp.makeUnary();
        // nodes of the graph are elements not belonging to X
        DGraph pred = new DGraph();
        TreeMap<Comparable, Node> nodeCreated = new TreeMap<Comparable, Node>();
        for (Comparable x : tmp.getSet()) {
            Node n = new Node(x);
            pred.addNode(n);
            nodeCreated.put(x, n);
        }
        // an edge is added from b to a when there exists a rule X+a -> b or a -> b
        for (Rule rule : tmp.getRules()) {
            for (Object a : rule.getPremise()) {
                ComparableSet diff = new ComparableSet(rule.getPremise());
                diff.remove(a);
                Node from = nodeCreated.get(rule.getConclusion().first());
                Node to = nodeCreated.get(a);
                Edge ed;
                if (pred.containsEdge(from, to)) {
                    ed = pred.getEdge(from, to);
                } else {
                    ed = new Edge(from, to, new TreeSet<ComparableSet>());
                    pred.addEdge(ed);
                }
                ((TreeSet<ComparableSet>) ed.getContent()).add(diff);
            }
        }
        return pred;
    }

    /**
     * Returns the dependency graph of this component.
     *
     * Dependency graph of this component is the representative graph of the
     * canonical direct basis. Therefore, the canonical direct basis has to be
     * generated before to compute its representativ graph, and this treatment
     * is performed in O(d), as for the canonical direct basis generation, where
     * d corresponds to the number of rules that have to be added by the direct
     * treatment. This number is exponential in the worst case.
     *
     * @return the dependency graph of this component.
     */
    public DGraph dependencyGraph() {
        ImplicationalSystem bcd = new ImplicationalSystem(this);
        bcd.makeCanonicalDirectBasis();
        bcd.makeUnary();
        return bcd.representativeGraph();
    }

    /**
     * Removes from this component reducible elements.
     *
     * Reducible elements are elements equivalent by closure to others elements.
     * They are computed by `getReducibleElements` of `ClosureSystem` in
     * O(O(|Sigma||S|^2)
     *
     * @return the set of reducibles removed elements, with their equivalent
     * elements
     */
    public TreeMap<Comparable, TreeSet<Comparable>> reduction() {
        // compute the reducible elements
        TreeMap red = this.getReducibleElements();
        // collect elements implied by nothing
        TreeSet<Comparable> truth = this.closure(new TreeSet<Comparable>());
        // modify each rule
        for (Object x : red.keySet()) {
            TreeSet<Rule> rules = this.sigma;
            rules = (TreeSet<Rule>) rules.clone();
            for (Rule rule : rules) {
                Rule rule2 = new Rule();
                boolean modif = false;
                // replace the reducible element by its equivalent in the premise
                TreeSet premise = rule.getPremise();
                premise = (TreeSet) premise.clone();
                if (premise.contains(x)) {
                    premise.remove(x);
                    premise.addAll((TreeSet) red.get(x));
                    rule2.addAllToPremise(premise);
                    modif = true;
                } else {
                    rule2.addAllToPremise(premise);
                }
                // replace the reducible element by its equivalent in the conclusion
                TreeSet conclusion = rule.getConclusion();
                conclusion = (TreeSet) conclusion.clone();
                if (conclusion.contains(x)) {
                    conclusion.remove(x);
                    conclusion.addAll((TreeSet) red.get(x));
                    rule2.addAllToConclusion(conclusion);
                    modif = true;
                } else {
                    rule2.addAllToConclusion(conclusion);
                }
                // replace the rule if modified
                if (modif) {
                    if (truth.containsAll(rule2.getConclusion())) {
                        this.removeRule(rule); // Conclusions of this rule are always true, thus the rule is useless
                    } else {
                        this.replaceRule(rule, rule2);
                    }
                } else if (truth.containsAll(rule.getConclusion())) {
                    this.removeRule(rule); // Conclusions of this rule are always true, thus the rule is useless
                }
            }
            // remove the reducible elements from the elements set
            this.deleteElement((Comparable) x);
        }
        return red;
    }

    /**
     * Return true if this component is reduced.
     *
     * @return true if this component is reduced.
     */
    public boolean isReduced() {
        // Copy this component not to modify it
        ImplicationalSystem tmp = new ImplicationalSystem(this);
        return tmp.reduction().isEmpty();
    }

    /*
     * --------------- IMPLEMENTATION OF CLOSURESYSTEM ABSTRACT METHODS ------------
     */

    /**
     * Builds the closure of a set X of indexed elements.
     *
     * The closure is initialised with X. The closure is incremented with the
     * conclusion of each rule whose premise is included in it. Iterations over
     * the rules are performed until no new element has to be added in the
     * closure.
     *
     * For direct ImplicationalSystem, only one iteration is needed, and the
     * treatment is performed in O(|Sigma||S|).
     *
     * For non direct ImplicationalSystem, at most |S| iterations are needed,
     * and this tratment is performed in O(|Sigma||S|^2).
     *
     * @param x a TreeSet of indexed elements
     *
     * @return the closure of X for this component
     */
    public TreeSet<Comparable> closure(TreeSet<Comparable> x) {
        TreeSet<Comparable> oldES = new TreeSet<Comparable>();
        // all the attributes are in their own closure
        TreeSet<Comparable> newES = new TreeSet<Comparable>(x);
        do {
            oldES.addAll(newES);
            for (Rule rule : this.sigma) {
                if (newES.containsAll(rule.getPremise()) || rule.getPremise().isEmpty()) {
                    newES.addAll(rule.getConclusion());
                }
            }
        } while (!oldES.equals(newES));
        return newES;
    }
}
