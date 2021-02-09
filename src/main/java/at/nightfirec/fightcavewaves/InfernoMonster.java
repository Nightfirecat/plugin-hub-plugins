/*
 * Copyright (c) 2020, Jordan Atwood <nightfirecat@protonmail.com>
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
package at.nightfirec.fightcavewaves;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
enum InfernoMonster implements WaveMonster
{
	JAL_NIB("Jal-Nib", WaveMonsterType.NIBBLER, 32, 3),
	JAL_MEJRAH("Jal-MejRah", WaveMonsterType.BAT, 85),
	JAL_AK("Jal-Ak", WaveMonsterType.BLOB, 165),
	JAL_IMKOT("Jal-ImKot", WaveMonsterType.MELEE, 240),
	JAL_XIL("Jal-Xil", WaveMonsterType.RANGER, 370),
	JAL_ZEK("Jal-Zek", WaveMonsterType.MAGE, 490),
	JALTOK_JAD("JalTok-Jad", WaveMonsterType.JAD, 900),
	TZKAL_ZUK("TzKal-Zuk", WaveMonsterType.ZUK, 1400);

	private final String name;
	private final WaveMonsterType type;
	private final int level;
	private final int countPerSpawn;

	InfernoMonster(final String name, final WaveMonsterType type, final int level)
	{
		this(name, type, level, 1);
	}
}
