/*
 * This file is part of SparkTrail 3.
 *
 * SparkTrail 3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SparkTrail 3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SparkTrail 3.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsh105.sparktrail.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;

public class InputPrompt extends ValidatingPrompt {

      private InputFunction function;

      public InputPrompt(InputFunction function) {
            this.function = function;
      }

      @Override
      protected boolean isInputValid(ConversationContext conversationContext, String s) {
            return this.function.isValid(conversationContext, s);
      }

      @Override
      protected Prompt acceptValidatedInput(ConversationContext conversationContext, String s) {
            this.function.function(conversationContext, s);
            return new InputSuccessPrompt(this.function.getSuccessMessage());
      }

      @Override
      public String getPromptText(ConversationContext conversationContext) {
            return this.function.getPromptText();
      }

      @Override
      protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return this.function.getFailedText();
      }
}
