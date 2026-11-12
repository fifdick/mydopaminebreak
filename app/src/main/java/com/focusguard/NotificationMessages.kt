package com.focusguard

/**
 * NotificationMessages — 150 messages for the test period before Claude API is live.
 *
 * Organized into 5 tone categories, 30 each:
 *   BLUNT       — direct, no nonsense
 *   ABSURDIST   — weird, unexpected, algorithm-aware
 *   PHILOSOPHICAL — questions that linger
 *   DARK_HUMOR  — honest and slightly grim
 *   IMPULSE     — speaks directly to the impulse pattern
 *
 * getRandomMessage() weights toward unused messages first,
 * so you won't see repeats until the full list is cycled.
 */
object NotificationMessages {

    enum class Tone { BLUNT, ABSURDIST, PHILOSOPHICAL, DARK_HUMOR, IMPULSE }

    data class Message(val text: String, val tone: Tone)

    private val all: List<Message> = listOf(

        // ── BLUNT ──────────────────────────────────────────────────────────────
        Message("Nothing's there. Put it down.", Tone.BLUNT),
        Message("You've checked this already. Twice.", Tone.BLUNT),
        Message("Stop. Seriously.", Tone.BLUNT),
        Message("This is the problem.", Tone.BLUNT),
        Message("You're doing it again.", Tone.BLUNT),
        Message("App five. Still nothing.", Tone.BLUNT),
        Message("Put it face down. Now.", Tone.BLUNT),
        Message("You don't need this right now.", Tone.BLUNT),
        Message("This is the loop. Step out.", Tone.BLUNT),
        Message("Close it.", Tone.BLUNT),
        Message("You know this won't help.", Tone.BLUNT),
        Message("The phone isn't the answer today.", Tone.BLUNT),
        Message("Three apps in four minutes. Stop.", Tone.BLUNT),
        Message("It's not going to get interesting.", Tone.BLUNT),
        Message("You're running from something. Stop running.", Tone.BLUNT),
        Message("Nothing changed since last time.", Tone.BLUNT),
        Message("You already know how this ends.", Tone.BLUNT),
        Message("Whatever you're avoiding, face it.", Tone.BLUNT),
        Message("This isn't rest. This is avoidance.", Tone.BLUNT),
        Message("There's nothing to find in there.", Tone.BLUNT),
        Message("Your hand picked it up. You didn't.", Tone.BLUNT),
        Message("This is unconscious. Wake up.", Tone.BLUNT),
        Message("Four apps. Zero reasons.", Tone.BLUNT),
        Message("You grabbed it on autopilot again.", Tone.BLUNT),
        Message("The boredom isn't in the phone.", Tone.BLUNT),
        Message("It's not doing what you want it to do.", Tone.BLUNT),
        Message("Unlock, scroll, close. That's not a plan.", Tone.BLUNT),
        Message("Wrong direction again.", Tone.BLUNT),
        Message("Still here? Put it down.", Tone.BLUNT),
        Message("The scroll ends the same way every time.", Tone.BLUNT),

        // ── ABSURDIST ──────────────────────────────────────────────────────────
        Message("The sixth app won't have the thing either.", Tone.ABSURDIST),
        Message("Plot twist: there's nothing there.", Tone.ABSURDIST),
        Message("Congratulations. You've seen everything. Again.", Tone.ABSURDIST),
        Message("Somewhere, a server is judging you.", Tone.ABSURDIST),
        Message("This app also contains disappointment.", Tone.ABSURDIST),
        Message("Fun fact: you'll forget this in 90 seconds.", Tone.ABSURDIST),
        Message("The reel after this one is also hollow.", Tone.ABSURDIST),
        Message("You're feeding a machine that doesn't care.", Tone.ABSURDIST),
        Message("Breaking news: you already checked this.", Tone.ABSURDIST),
        Message("What if the phone was the villain all along.", Tone.ABSURDIST),
        Message("The answer was never in the rectangle.", Tone.ABSURDIST),
        Message("App number seven is also not it.", Tone.ABSURDIST),
        Message("Scientists confirm: nothing new on the feed.", Tone.ABSURDIST),
        Message("You've unlocked: opened for no reason.", Tone.ABSURDIST),
        Message("The thing you're looking for isn't digital.", Tone.ABSURDIST),
        Message("Loading... nope. Still empty.", Tone.ABSURDIST),
        Message("Spoiler: the scroll doesn't end.", Tone.ABSURDIST),
        Message("Error 404: purpose not found in this app.", Tone.ABSURDIST),
        Message("This app is also a trap. Just so you know.", Tone.ABSURDIST),
        Message("You and the algorithm, alone again.", Tone.ABSURDIST),
        Message("The phone picked you up this time.", Tone.ABSURDIST),
        Message("Meanwhile, the view exists without a screen.", Tone.ABSURDIST),
        Message("Your brain ordered dopamine. Wrong address.", Tone.ABSURDIST),
        Message("This has been a journey. A pointless one.", Tone.ABSURDIST),
        Message("The reel machine is fed. You are not.", Tone.ABSURDIST),
        Message("Currently: hunting for nothing in particular.", Tone.ABSURDIST),
        Message("You've been scrolling since before you knew it.", Tone.ABSURDIST),
        Message("The algorithm thanks you. You're welcome.", Tone.ABSURDIST),
        Message("Five apps. One question: for what?", Tone.ABSURDIST),
        Message("The notification you opened this for doesn't exist.", Tone.ABSURDIST),

        // ── PHILOSOPHICAL ──────────────────────────────────────────────────────
        Message("What were you actually looking for?", Tone.PHILOSOPHICAL),
        Message("What feeling were you trying to avoid?", Tone.PHILOSOPHICAL),
        Message("What would you do if the phone wasn't there?", Tone.PHILOSOPHICAL),
        Message("Is this how you want this hour remembered?", Tone.PHILOSOPHICAL),
        Message("What does your body actually need right now?", Tone.PHILOSOPHICAL),
        Message("The present moment is happening without you.", Tone.PHILOSOPHICAL),
        Message("Boredom is a signal, not a problem.", Tone.PHILOSOPHICAL),
        Message("What's the real question underneath this?", Tone.PHILOSOPHICAL),
        Message("Time is the one thing you can't get back.", Tone.PHILOSOPHICAL),
        Message("Who are you when the screen is off?", Tone.PHILOSOPHICAL),
        Message("What would feel better than this, honestly?", Tone.PHILOSOPHICAL),
        Message("The discomfort you're avoiding is information.", Tone.PHILOSOPHICAL),
        Message("Is this rest, or is this hiding?", Tone.PHILOSOPHICAL),
        Message("Your attention is your most valuable thing.", Tone.PHILOSOPHICAL),
        Message("What memory are you making right now?", Tone.PHILOSOPHICAL),
        Message("Notice what you felt just before you picked it up.", Tone.PHILOSOPHICAL),
        Message("The life you want is not in there.", Tone.PHILOSOPHICAL),
        Message("What would presence feel like right now?", Tone.PHILOSOPHICAL),
        Message("You had a thought before this. What was it?", Tone.PHILOSOPHICAL),
        Message("Where does this hour go when it's gone?", Tone.PHILOSOPHICAL),
        Message("What are you actually hungry for?", Tone.PHILOSOPHICAL),
        Message("This moment won't come back.", Tone.PHILOSOPHICAL),
        Message("What would you do with one extra hour today?", Tone.PHILOSOPHICAL),
        Message("The stillness you're avoiding is where you live.", Tone.PHILOSOPHICAL),
        Message("What does it mean that your hand moved first?", Tone.PHILOSOPHICAL),
        Message("There's a version of this hour without the phone.", Tone.PHILOSOPHICAL),
        Message("What were you thinking about before this?", Tone.PHILOSOPHICAL),
        Message("What's one real thing you could do instead?", Tone.PHILOSOPHICAL),
        Message("Your brain went somewhere. Follow it back.", Tone.PHILOSOPHICAL),
        Message("The feeling passed the second you picked it up.", Tone.PHILOSOPHICAL),

        // ── DARK HUMOR ─────────────────────────────────────────────────────────
        Message("Congrats. Three minutes gone. Nothing happened.", Tone.DARK_HUMOR),
        Message("Future you will also not remember this scroll.", Tone.DARK_HUMOR),
        Message("That time is gone now. Hope it was worth it.", Tone.DARK_HUMOR),
        Message("You're paying attention rent to a tech company.", Tone.DARK_HUMOR),
        Message("Another app. Another small piece of the afternoon.", Tone.DARK_HUMOR),
        Message("The phone won again.", Tone.DARK_HUMOR),
        Message("This is fine. Totally fine.", Tone.DARK_HUMOR),
        Message("In ten years you won't remember a single reel.", Tone.DARK_HUMOR),
        Message("You opened this before your brain voted.", Tone.DARK_HUMOR),
        Message("Somewhere, a VC is happy you're doing this.", Tone.DARK_HUMOR),
        Message("Your focus left the building. Quietly.", Tone.DARK_HUMOR),
        Message("Another two minutes the day didn't get back.", Tone.DARK_HUMOR),
        Message("Technically you're alive. More presence available though.", Tone.DARK_HUMOR),
        Message("Your dopamine is on loan to someone else's algorithm.", Tone.DARK_HUMOR),
        Message("Great session. Zero memories made.", Tone.DARK_HUMOR),
        Message("This is the app that ate Thursday.", Tone.DARK_HUMOR),
        Message("Not your proudest moment.", Tone.DARK_HUMOR),
        Message("Five apps opened. None of them remembered.", Tone.DARK_HUMOR),
        Message("The afternoon is going somewhere. Not sure you are.", Tone.DARK_HUMOR),
        Message("At least you're consistent.", Tone.DARK_HUMOR),
        Message("Each scroll is a small vote for this version of your day.", Tone.DARK_HUMOR),
        Message("The phone is winning on points right now.", Tone.DARK_HUMOR),
        Message("Today's highlight reel: mostly phone, some regret.", Tone.DARK_HUMOR),
        Message("The machine is patient. Are you?", Tone.DARK_HUMOR),
        Message("This notification brought to you by your own behavior data.", Tone.DARK_HUMOR),
        Message("Cool. Five things opened, none of them finished.", Tone.DARK_HUMOR),
        Message("The scroll never ends. Neither does the lost time.", Tone.DARK_HUMOR),
        Message("Eight minutes to the algorithm. It's grateful.", Tone.DARK_HUMOR),
        Message("This is your brain on boredom. Could be better.", Tone.DARK_HUMOR),
        Message("You gave the phone your attention. It gave you nothing back.", Tone.DARK_HUMOR),

        // ── IMPULSE ────────────────────────────────────────────────────────────
        Message("You picked it up mid-thought. What was the thought?", Tone.IMPULSE),
        Message("Your brain wanted stimulation. This isn't the right kind.", Tone.IMPULSE),
        Message("Task-switch detected. Was the first task actually done?", Tone.IMPULSE),
        Message("The dopamine hit is fake. The time loss is real.", Tone.IMPULSE),
        Message("You left something unfinished to open this.", Tone.IMPULSE),
        Message("Impulse: 1. Your intentions: 0. Not today.", Tone.IMPULSE),
        Message("The boredom spike brought you here. It lied.", Tone.IMPULSE),
        Message("Your brain fired a craving. You don't have to obey it.", Tone.IMPULSE),
        Message("Something felt hard. This felt easy. Notice that.", Tone.IMPULSE),
        Message("You don't have to follow every impulse today.", Tone.IMPULSE),
        Message("The task is still there. So are you.", Tone.IMPULSE),
        Message("Full focus available. Currently aimed at the wrong thing.", Tone.IMPULSE),
        Message("Your brain skipped ahead without you.", Tone.IMPULSE),
        Message("The urge passes if you wait 90 seconds. Try it.", Tone.IMPULSE),
        Message("You were somewhere else. Come back.", Tone.IMPULSE),
        Message("Distraction chosen. Was it yours or the phone's?", Tone.IMPULSE),
        Message("The impulse spike lasts 90 seconds. Outlast it.", Tone.IMPULSE),
        Message("You can choose the next thought. This one wasn't it.", Tone.IMPULSE),
        Message("Your attention went somewhere. Find it.", Tone.IMPULSE),
        Message("The impulse is not the plan.", Tone.IMPULSE),
        Message("Something avoided you into opening this.", Tone.IMPULSE),
        Message("You had momentum somewhere. Where did it go?", Tone.IMPULSE),
        Message("The real task is still possible. This is optional.", Tone.IMPULSE),
        Message("Quick check turned into this. As expected.", Tone.IMPULSE),
        Message("Your brain needed a break. This isn't a break.", Tone.IMPULSE),
        Message("You can feel restless without feeding it.", Tone.IMPULSE),
        Message("The next hour is still yours if you take it back.", Tone.IMPULSE),
        Message("Impulse control is hard. But you caught it this time.", Tone.IMPULSE),
        Message("Name what you were avoiding. Then go back.", Tone.IMPULSE),
        Message("You noticed. That's the whole trick. Now put it down.", Tone.IMPULSE)
    )

    // ─── Tracking which messages have been shown ───────────────────────────────

    private val shownIndices = mutableSetOf<Int>()

    /**
     * Returns a random message, prioritizing ones not yet shown.
     * Once all 150 are cycled, resets and starts again — still random each time.
     */
    fun getRandomMessage(): Message {
        val available = all.indices.filter { it !in shownIndices }
        val pool = if (available.isEmpty()) {
            shownIndices.clear()
            all.indices.toList()
        } else {
            available
        }
        val chosen = pool.random()
        shownIndices.add(chosen)
        return all[chosen]
    }

    /**
     * Get a message from a specific tone if you want to
     * vary tone intentionally (e.g., philosophical in morning, blunt at work).
     */
    fun getRandomMessageByTone(tone: Tone): Message {
        return all.filter { it.tone == tone }.random()
    }

    /** How many unique messages remain before the cycle resets. */
    fun remaining(): Int = all.size - shownIndices.size

    /** Full count. */
    val count: Int get() = all.size
}
