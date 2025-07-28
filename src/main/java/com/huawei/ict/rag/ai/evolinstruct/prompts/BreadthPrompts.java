package com.huawei.ict.rag.ai.evolinstruct.prompts;

public class BreadthPrompts {
    private static final String BASE_INSTRUCTION =
            """
                    I want you act as a Prompt Creator.
                    Your goal is to draw inspiration from the #Given Prompt# to create a brand new prompt.
                    This new prompt should belong to the same domain as the #Given Prompt# but be even more rare.
                    The LENGTH and complexity of the #Created Prompt# should be similar to that of the #Given Prompt#.
                    The #Created Prompt# must be reasonable and must be understood and responded by humans.
                    '#Given Prompt#', '#Created Prompt#', 'given prompt' and 'created prompt' are not allowed to appear in #Created Prompt#
                    """;

    public static String createBreadthPrompt(String instruction) {
        return BASE_INSTRUCTION +
                "#Given Prompt#: \n" + instruction + "\n" +
                "#Created Prompt#:\n";
    }
}
