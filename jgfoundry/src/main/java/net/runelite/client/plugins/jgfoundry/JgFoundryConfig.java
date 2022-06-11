/*
 * Copyright (c) 2018, Andrew EP | ElPinche256 <https://github.com/ElPinche256>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.jgfoundry;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.*;

@ConfigGroup("JgFoundry")

public interface JgFoundryConfig extends Config
{

	@ConfigSection(
			keyName = "instructionsTitle",
			name = "Instructions",
			description = "",
			position = 0
	)
	String instructionsTitle = "instructionsTitle";

	@ConfigItem(
			keyName = "instructions",
			name = "",
			description = "Instructions. Don't enter anything into this field",
			position = 0,
			section = "instructionsTitle"
	)
	default String instructions()
	{
		return "Test";
	}

	@ConfigSection(
			keyName = "JgTestTitle",
			name = "JgTest",
			description = "",
			position = 1
	)
	String elTestTitle = "JgTestTitle";

	@ConfigItem(
			keyName = "X",
			name = "X",
			description = "WorldPoint",
			position = 2,
			section = "JgTestTitle"
	)
	default int X()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "Y",
			name = "Y",
			description = "WorldPoint",
			position = 3,
			section = "JgTestTitle"
	)
	default int Y()
	{
		return 0;
	}

	@ConfigSection(
			keyName = "uiTitle",
			name = "UI Config",
			description = "",
			position = 140
	)
	String uiTitle = "uiTitle";

	@ConfigItem(
			keyName = "enableUI",
			name = "Enable UI",
			description = "Enable to turn on in game UI",
			position = 140,
			section = "uiTitle"
	)
	default boolean enableUI()
	{
		return true;
	}

	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "Test button that changes variable value",
			position = 150
	)
	default Button startButton()
	{
		return new Button();
	}
}