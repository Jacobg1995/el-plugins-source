package net.runelite.client.plugins.elutils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LegacyMenuEntry
{
	String option;
	String target;
	int identifier;
	int menuAction;
	int param0;
	int param1;
	boolean forceLeftClick;
}