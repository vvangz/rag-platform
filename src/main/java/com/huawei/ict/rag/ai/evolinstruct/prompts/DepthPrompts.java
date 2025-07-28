package com.huawei.ict.rag.ai.evolinstruct.prompts;

import java.util.Random;

public class DepthPrompts {
    private static final String BASE_INSTRUCTION =
            "I want you act as a Prompt Rewriter.\n" +
                    "Your objective is to rewrite a given prompt into a more complex version to make those famous AI systems (e.g., chatgpt and GPT4) a bit harder to handle.\n" +
                    "But the rewritten prompt must be reasonable and must be understood and responded by humans.\n" +
                    "Your rewriting cannot omit the non-text parts such as the table and code in #The Given Prompt#:. Also, please do not omit the input in #The Given Prompt#. \n" +
                    "You SHOULD complicate the given prompt using the following method: \n" +
                    "%s \n" +
                    "You should try your best not to make the #Rewritten Prompt# become verbose, #Rewritten Prompt# can only add 10 to 20 words into #The Given Prompt#. \n" +
                    "'#The Given Prompt#', '#Rewritten Prompt#', 'given prompt' and 'rewritten prompt' are not allowed to appear in #Rewritten Prompt#\n";

    public static String createConstraintsPrompt(String instruction) {
        String method = "Please add one more constraints/requirements into #The Given Prompt#'";
        return String.format(BASE_INSTRUCTION, method) +
                "#The Given Prompt#: \n" + instruction + "\n" +
                "#Rewritten Prompt#:\n";
    }

    public static String createDeepenPrompt(String instruction) {
        String method = "If #The Given Prompt# contains inquiries about certain issues, the depth and breadth of the inquiry can be increased.";
        return String.format(BASE_INSTRUCTION, method) +
                "#The Given Prompt#: \n" + instruction + "\n" +
                "#Rewritten Prompt#:\n";
    }

    public static String createConcretizingPrompt(String instruction) {
        String method = "Please replace general concepts with more specific concepts.";
        return String.format(BASE_INSTRUCTION, method) +
                "#The Given Prompt#: \n" + instruction + "\n" +
                "#Rewritten Prompt#:\n";
    }

    public static String createReasoningPrompt(String instruction) {
        String method = "If #The Given Prompt# can be solved with just a few simple thinking processes, you can rewrite it to explicitly request multiple-step reasoning.";
        return String.format(BASE_INSTRUCTION, method) +
                "#The Given Prompt#: \n" + instruction + "\n" +
                "#Rewritten Prompt#:\n";
    }

    public static String createRandomDepthPrompt(String instruction) {
        PromptType[] types = PromptType.values();
        PromptType randomType = types[new Random().nextInt(types.length)];

        return switch (randomType) {
            case CONSTRAINTS -> createConstraintsPrompt(instruction);
            case DEEPEN -> createDeepenPrompt(instruction);
            case CONCRETIZING -> createConcretizingPrompt(instruction);
            case REASONING -> createReasoningPrompt(instruction);
        };
    }
}