package org.protempa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseConfiguration.AssertBehaviour;

abstract class AbstractExecutionStrategy implements ExecutionStrategy {

    /**
     * The {@link AbstractionFinder} using this execution strategy.
     */
    private final AbstractionFinder abstractionFinder;
    protected RuleBase ruleBase;

    /**
     * @param abstractionFinder
     *            the {@link AbstractionFinder} using this execution strategy
     */
    AbstractExecutionStrategy(AbstractionFinder abstractionFinder) {
        this.abstractionFinder = abstractionFinder;
    }

    protected final AbstractionFinder getAbstractionFinder() {
        return this.abstractionFinder;
    }

    @Override
    public void createRuleBase(Set<String> propIds,
            DerivationsBuilder listener, QuerySession qs)
            throws ProtempaException {
        ValidateAlgorithmCheckedVisitor visitor = new ValidateAlgorithmCheckedVisitor(
                this.abstractionFinder.getAlgorithmSource());
        JBossRuleCreator ruleCreator = new JBossRuleCreator(
                visitor.getAlgorithms(), listener);
        List<PropositionDefinition> propDefs = new ArrayList<PropositionDefinition>(
                propIds.size());
        for (String propId : propIds) {
            PropositionDefinition propDef = this.abstractionFinder
                    .getKnowledgeSource().readPropositionDefinition(propId);
            if (propDef != null) {
                propDefs.add(propDef);
            } else {
                throw new FinderException("Invalid proposition id: " + propId);
            }
        }
        if (propIds != null) {
            Set<PropositionDefinition> result = new HashSet<PropositionDefinition>();
            aggregateChildren(visitor, result, propDefs);
            ruleCreator.visit(result);
        }
        this.ruleBase = new JBossRuleBaseFactory(ruleCreator,
                createRuleBaseConfiguration(ruleCreator)).newInstance();
    }

    protected RuleBaseConfiguration createRuleBaseConfiguration(
            JBossRuleCreator ruleCreator)
            throws PropositionDefinitionInstantiationException {
        RuleBaseConfiguration config = new RuleBaseConfiguration();
        config.setShadowProxy(false);
        try {
            config.setConflictResolver(new PROTEMPAConflictResolver(
                    this.abstractionFinder.getKnowledgeSource(), ruleCreator
                            .getRuleToAbstractionDefinitionMap()));
        } catch (KnowledgeSourceReadException ex) {
            throw new PropositionDefinitionInstantiationException(
                    "Could not instantiate proposition definitions", ex);
        }
        config.setAssertBehaviour(AssertBehaviour.EQUALITY);
        return config;
    }
    
    /**
     * Collect all of the propositions for which we need to create rules.
     * 
     * @param algorithms
     *            an empty {@link Map} that will be populated with algorithms
     *            for each proposition definition for which a rule will be
     *            created.
     * @param result
     *            an empty {@link Set} that will be populated with the
     *            proposition definitions for which rules will be created.
     * @param propIds
     *            the proposition id {@link String}s to be found.
     * @throws org.protempa.ProtempaException
     *             if an error occurs reading the algorithm specified by a
     *             proposition definition.
     */
    private void aggregateChildren(
            ValidateAlgorithmCheckedVisitor validatorVisitor,
            Set<PropositionDefinition> result,
            List<PropositionDefinition> propDefs) throws ProtempaException {
        DirectChildrenVisitor dcVisitor = new DirectChildrenVisitor(
                this.abstractionFinder.getKnowledgeSource());
        for (PropositionDefinition propDef : propDefs) {
            propDef.acceptChecked(validatorVisitor);
            propDef.acceptChecked(dcVisitor);
            result.add(propDef);
            aggregateChildren(validatorVisitor, result,
                    dcVisitor.getDirectChildren());
            dcVisitor.clear();
        }
    }

    private class ValidateAlgorithmCheckedVisitor extends
            AbstractPropositionDefinitionCheckedVisitor {

        private final Map<LowLevelAbstractionDefinition, Algorithm> algorithms;
        private final AlgorithmSource algorithmSource;

        ValidateAlgorithmCheckedVisitor(AlgorithmSource algorithmSource) {
            this.algorithms = new HashMap<LowLevelAbstractionDefinition, Algorithm>();
            this.algorithmSource = algorithmSource;
        }

        Map<LowLevelAbstractionDefinition, Algorithm> getAlgorithms() {
            return this.algorithms;
        }

        @Override
        public void visit(
                LowLevelAbstractionDefinition lowLevelAbstractionDefinition)
                throws ProtempaException {
            String algorithmId = lowLevelAbstractionDefinition.getAlgorithmId();
            Algorithm algorithm = algorithmSource.readAlgorithm(algorithmId);
            if (algorithm == null && algorithmId != null) {
                throw new NoSuchAlgorithmException(
                        "Low level abstraction definition "
                                + lowLevelAbstractionDefinition.getId()
                                + " wants the algorithm " + algorithmId
                                + ", but no such algorithm is available.");
            }
            this.algorithms.put(lowLevelAbstractionDefinition, algorithm);

        }

        @Override
        public void visit(EventDefinition eventDefinition)
                throws ProtempaException {
        }

        @Override
        public void visit(
                HighLevelAbstractionDefinition highLevelAbstractionDefinition)
                throws ProtempaException {
        }

        @Override
        public void visit(
                PrimitiveParameterDefinition primitiveParameterDefinition)
                throws ProtempaException {
        }

        @Override
        public void visit(SliceDefinition sliceAbstractionDefinition)
                throws ProtempaException {
        }

        @Override
        public void visit(PairDefinition pairAbstractionDefinition)
                throws ProtempaException {
        }

        @Override
        public void visit(ConstantDefinition def) throws ProtempaException {
        }
    }
}