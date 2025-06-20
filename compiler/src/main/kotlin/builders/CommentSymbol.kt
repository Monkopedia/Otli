/**
 * Copyright (C) 2024 Jason Monk <monkopedia@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.otli.builders

class CommentSymbol(private val content: String) : Symbol {
    override val blockSemi: Boolean
        get() = true

    override fun build(builder: CodeStringBuilder) {
        var isFirst = true
        for (line in content.split("\n")) {
            if (isFirst) {
                isFirst = false
            } else {
                builder.append('\n')
            }
            builder.append("// $line")
        }
    }

    override fun toString(): String = "COMMENT[$content]"
}

inline fun CodeBuilder.comment(str: String) = addSymbol(CommentSymbol(str))
