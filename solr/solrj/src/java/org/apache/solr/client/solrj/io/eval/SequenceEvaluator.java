/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.client.solrj.io.eval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.MathArrays;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.stream.expr.Explanation;
import org.apache.solr.client.solrj.io.stream.expr.Explanation.ExpressionType;
import org.apache.solr.client.solrj.io.stream.expr.Expressible;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionParameter;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;

public class SequenceEvaluator extends ComplexEvaluator implements Expressible {

  private static final long serialVersionUID = 1;

  public SequenceEvaluator(StreamExpression expression, StreamFactory factory) throws IOException {
    super(expression, factory);
  }

  public List<Number> evaluate(Tuple tuple) throws IOException {

    if(subEvaluators.size() != 3) {
      throw new IOException("Sequence evaluator expects 3 parameters found: "+subEvaluators.size());
    }

    StreamEvaluator sizeEval = subEvaluators.get(0);
    StreamEvaluator startEval = subEvaluators.get(1);
    StreamEvaluator strideEval = subEvaluators.get(2);

    Number sizeNum = (Number)sizeEval.evaluate(tuple);
    Number startNum = (Number)startEval.evaluate(tuple);
    Number strideNum = (Number)strideEval.evaluate(tuple);

    int[] sequence = MathArrays.sequence(sizeNum.intValue(), startNum.intValue(), strideNum.intValue());
    List<Number> numbers = new ArrayList(sequence.length);
    for(int i : sequence) {
      numbers.add(i);
    }

    return numbers;
  }

  @Override
  public StreamExpressionParameter toExpression(StreamFactory factory) throws IOException {
    StreamExpression expression = new StreamExpression(factory.getFunctionName(getClass()));
    return expression;
  }

  @Override
  public Explanation toExplanation(StreamFactory factory) throws IOException {
    return new Explanation(nodeId.toString())
        .withExpressionType(ExpressionType.EVALUATOR)
        .withFunctionName(factory.getFunctionName(getClass()))
        .withImplementingClass(getClass().getName())
        .withExpression(toExpression(factory).toString());
  }
}