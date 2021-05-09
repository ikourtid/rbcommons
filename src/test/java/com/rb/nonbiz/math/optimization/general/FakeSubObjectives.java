package com.rb.nonbiz.math.optimization.general;

import com.rb.nonbiz.text.Strings;

public class FakeSubObjectives {

  private abstract static class FakeSubObjective extends LinearSubObjectiveFunction {

    protected FakeSubObjective(HighLevelVarExpression highLevelVarExpression) {
      super(highLevelVarExpression);
    }

    @Override
    public String toMultilineString() {
      // Not needed, but we might as well make this consistent with the prod subobjectives.
      return Strings.format("[FakeSO %s FakeSO]", getHighLevelVarExpression().toMultilineString());
    }

  }

  // I am creating these fake concrete classes because (as of Nov 2016)
  // there aren't enough real subObjective function classes in our LP formulation
  // to allow our tests here to use different classes.
  public static class FakeSubObjective1 extends FakeSubObjective {

    private FakeSubObjective1(HighLevelVarExpression highLevelVarExpression) {
      super(highLevelVarExpression);
    }

  }


  public static class FakeSubObjective2 extends FakeSubObjective {

    private FakeSubObjective2(HighLevelVarExpression highLevelVarExpression) {
      super(highLevelVarExpression);
    }

  }


  public static class FakeSubObjective3 extends FakeSubObjective {

    private FakeSubObjective3(HighLevelVarExpression highLevelVarExpression) {
      super(highLevelVarExpression);
    }

  }


  public static class FakeSubObjective4 extends FakeSubObjective {

    private FakeSubObjective4(HighLevelVarExpression highLevelVarExpression) {
      super(highLevelVarExpression);
    }

  }


  public static class FakeConcreteObjective extends SimpleLinearObjectiveFunction {

    private FakeConcreteObjective(HighLevelVarExpression highLevelVarExpression) {
      super(highLevelVarExpression);
    }

    public static FakeConcreteObjective fakeConcreteObjective(HighLevelVarExpression highLevelVarExpression) {
      return new FakeConcreteObjective(highLevelVarExpression);
    }

    @Override
    public <T> T visit(LinearObjectiveFunctionVisitor<T> visitor) {
      throw new IllegalArgumentException("not implemented");
    }

  }

}
