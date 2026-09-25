package org.walkwithgod.trivia;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(200)
public class TriviaSeedSeeder implements CommandLineRunner {

    private final TriviaSeedRepository seeds;

    public TriviaSeedSeeder(TriviaSeedRepository seeds) {
        this.seeds = seeds;
    }

    @Override
    public void run(String... args) {
        if (seeds.count() > 0)
            return;

        // ============================================================
        // EASY — well-known verses, foundational facts, simple truths
        // ============================================================
        List<TriviaSeed> easy = List.of(
                seed("VERSE_COMPLETE", "EASY",
                        "Complete the verse: 'For God so loved the world that he gave his ____'",
                        "only Son",
                        "holy Son,first Son,chosen Son",
                        "John 3:16 — the most quoted verse in the Bible.",
                        "John 3:16"),

                seed("VERSE_COMPLETE", "EASY",
                        "Complete the verse: 'The LORD is my ____; I shall not want.'",
                        "shepherd",
                        "strength,fortress,rock",
                        "Psalm 23 opens with the beloved shepherd metaphor.",
                        "Psalm 23:1"),

                seed("VERSE_SOURCE", "EASY",
                        "Which book of the Bible begins with 'In the beginning God created the heavens and the earth'?",
                        "Genesis",
                        "Exodus,Job,Psalms",
                        "Genesis 1:1 — the very first verse of the Bible.",
                        "Genesis 1:1"),

                seed("VERSE_SPEAKER", "EASY",
                        "Who said: 'I am the way, the truth, and the life'?",
                        "Jesus",
                        "Paul,Peter,John the Baptist",
                        "Jesus said this to Thomas in John 14:6.",
                        "John 14:6"),

                seed("LIFE_APPLICATION", "EASY",
                        "A friend is grieving the loss of a loved one. Which verse best comforts them?",
                        "'Blessed are those who mourn, for they will be comforted.'",
                        "'Rejoice always.' ,'Honor your father and mother.', 'The love of money is the root of all evil.'",
                        "Matthew 5:4 — a direct promise of comfort to those who mourn.",
                        "Matthew 5:4"),

                seed("LIFE_APPLICATION", "EASY",
                        "You feel anxious about tomorrow. Which verse best applies?",
                        "'Do not worry about tomorrow, for tomorrow will worry about itself.'",
                        "'An eye for an eye and a tooth for a tooth.', 'The Sabbath was made for man.', 'Let him who is without sin cast the first stone.'",
                        "Matthew 6:34 — Jesus' teaching on anxiety.",
                        "Matthew 6:34"),

                seed("VERSE_COMPLETE", "EASY",
                        "Complete the verse: 'Trust in the LORD with all your heart and lean not on your own ____.'",
                        "understanding",
                        "strength,wisdom,riches",
                        "Proverbs 3:5 — a cornerstone of Christian trust.",
                        "Proverbs 3:5"),

                seed("VERSE_SOURCE", "EASY",
                        "Which book tells the story of Noah and the ark?",
                        "Genesis",
                        "Exodus,Matthew,Psalms",
                        "Genesis 6–9 tells the account of Noah.",
                        "Genesis 6-9"),

                seed("VERSE_SOURCE", "EASY",
                        "Which Gospel tells the story of the Good Samaritan?",
                        "Luke",
                        "Matthew,Mark,John",
                        "The parable appears only in Luke 10.",
                        "Luke 10:25-37"),

                seed("VERSE_SPEAKER", "EASY",
                        "Who was the mother of Jesus?",
                        "Mary",
                        "Martha,Elizabeth,Sarah",
                        "Luke 1:30-31 records the angel's announcement to Mary.",
                        "Luke 1:31"),

                seed("VERSE_COMPLETE", "EASY",
                        "Complete the verse: 'I can do all things through ____ who strengthens me.'",
                        "Christ",
                        "faith,hope,love",
                        "Philippians 4:13 — often quoted for courage.",
                        "Philippians 4:13"),

                seed("VERSE_SPEAKER", "EASY",
                        "Who led the Israelites out of Egypt?",
                        "Moses",
                        "Abraham,David,Joshua",
                        "The Exodus account in the book of Exodus.",
                        "Exodus 3-14"),

                seed("LIFE_APPLICATION", "EASY",
                        "You want to know how to treat others. Which verse is the famous 'Golden Rule'?",
                        "'Do to others as you would have them do to you.'",
                        "'An eye for an eye.', 'Love only those who love you.', 'Judge and you will be judged.'",
                        "Luke 6:31 — the Golden Rule.",
                        "Luke 6:31"),

                seed("VERSE_SOURCE", "EASY",
                        "Which book contains the Ten Commandments?",
                        "Exodus",
                        "Leviticus,Numbers,Deuteronomy",
                        "Exodus 20 records the Ten Commandments given to Moses.",
                        "Exodus 20"),

                seed("VERSE_COMPLETE", "EASY",
                        "Complete: 'The Lord is my light and my ____; whom shall I fear?'",
                        "salvation",
                        "shepherd,strength,refuge",
                        "Psalm 27:1 — a bold declaration of trust.",
                        "Psalm 27:1"),

                seed("VERSE_SPEAKER", "EASY",
                        "Who baptized Jesus in the Jordan River?",
                        "John the Baptist",
                        "Peter,Paul,Andrew",
                        "Matthew 3:13-17 records the baptism.",
                        "Matthew 3:13-17"),

                seed("VERSE_SOURCE", "EASY",
                        "Which book records Jesus' birth?",
                        "Matthew",
                        "Acts,Romans,Revelation",
                        "Matthew 1-2 and Luke 2 both record the nativity.",
                        "Matthew 1-2"),

                seed("LIFE_APPLICATION", "EASY",
                        "A friend feels unforgivable. Which verse reminds them of God's mercy?",
                        "'If we confess our sins, he is faithful and just and will forgive us.'",
                        "'The wages of sin is death.', 'Be sure your sin will find you out.', 'The soul who sins shall die.'",
                        "1 John 1:9 — a promise of forgiveness.",
                        "1 John 1:9"),

                seed("VERSE_COMPLETE", "EASY",
                        "Complete: 'Your word is a ____ to my feet and a light to my path.'",
                        "lamp",
                        "sword,shield,rock",
                        "Psalm 119:105 — the famous verse on Scripture.",
                        "Psalm 119:105"),

                seed("VERSE_SPEAKER", "EASY",
                        "Who denied Jesus three times before the rooster crowed?",
                        "Peter",
                        "Judas,Thomas,Andrew",
                        "Recorded in all four Gospels.",
                        "Luke 22:54-62"),

                seed("LIFE_APPLICATION", "EASY",
                        "You are facing a big decision. Which verse best applies?",
                        "'In all your ways acknowledge him, and he will make straight your paths.'",
                        "'Do not be unequally yoked.', 'Pray without ceasing.', 'Give thanks in all circumstances.'",
                        "Proverbs 3:6 — trusting God for direction.",
                        "Proverbs 3:6"),

                seed("VERSE_SOURCE", "EASY",
                        "Which book tells of the walls of Jericho falling?",
                        "Joshua",
                        "Judges,1 Samuel,Ruth",
                        "Joshua 6 records the fall of Jericho.",
                        "Joshua 6"),

                seed("VERSE_COMPLETE", "EASY",
                        "Complete: 'Be strong and courageous... for the LORD your God will be with you ____ you go.'",
                        "wherever",
                        "whenever,unless,until",
                        "Joshua 1:9 — the verse quoted on the dashboard.",
                        "Joshua 1:9"),

                seed("VERSE_SPEAKER", "EASY",
                        "Which disciple doubted Jesus' resurrection until he saw the wounds?",
                        "Thomas",
                        "Peter,John,Philip",
                        "John 20:24-29 — 'Doubting Thomas'.",
                        "John 20:24-29"),

                seed("LIFE_APPLICATION", "EASY",
                        "Someone is struggling with jealousy. What does the Bible say?",
                        "'A heart at peace gives life to the body, but envy rots the bones.'",
                        "'Rejoice in the Lord always.', 'Honor the Sabbath.', 'Love your neighbor as yourself.'",
                        "Proverbs 14:30 — the destructive nature of envy.",
                        "Proverbs 14:30"),

                seed("VERSE_SOURCE", "EASY",
                        "Which New Testament book is a collection of letters to a young pastor?",
                        "1 Timothy",
                        "Romans,Hebrews,James",
                        "Paul wrote 1 and 2 Timothy to his protégé.",
                        "1 Timothy"),

                seed("VERSE_COMPLETE", "EASY",
                        "Complete: 'For all have sinned and fall short of the ____ of God.'",
                        "glory",
                        "law,grace,kingdom",
                        "Romans 3:23 — the universal need for salvation.",
                        "Romans 3:23"),

                seed("VERSE_SPEAKER", "EASY",
                        "Who wrote most of the letters in the New Testament?",
                        "Paul",
                        "Peter,John,James",
                        "Paul authored 13 epistles.",
                        "Romans-Philemon"),

                seed("LIFE_APPLICATION", "EASY",
                        "You have been hurt by a fellow believer. What does Jesus say to do?",
                        "'Forgive seventy times seven.'",
                        "'Distance yourself from them.', 'Report them to the elders immediately.', 'Never speak to them again.'",
                        "Matthew 18:22 — limitless forgiveness.",
                        "Matthew 18:22"),

                seed("VERSE_SOURCE", "EASY",
                        "Which Gospel emphasizes Jesus as the Word made flesh?",
                        "John",
                        "Matthew,Mark,Luke",
                        "John 1:14 — a unique opening to the fourth Gospel.",
                        "John 1:1-14"),

                seed("VERSE_COMPLETE", "EASY",
                        "Complete: 'The wages of sin is death, but the gift of God is ____ life in Christ Jesus.'",
                        "eternal",
                        "abundant,everlasting,resurrected",
                        "Romans 6:23 — sin's cost vs God's gift.",
                        "Romans 6:23"));

        // ============================================================
        // MEDIUM — familiar but nuanced; a step up from the basics
        // ============================================================
        List<TriviaSeed> medium = List.of(
                seed("VERSE_SOURCE", "MEDIUM",
                        "Which prophet was thrown into a den of lions?",
                        "Daniel",
                        "Ezekiel,Jeremiah,Isaiah",
                        "Daniel 6 records this event.",
                        "Daniel 6"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book tells of a man who lost everything but remained faithful to God?",
                        "Job",
                        "Psalms,Proverbs,Ecclesiastes",
                        "The book of Job explores undeserved suffering.",
                        "Job"),

                seed("VERSE_SPEAKER", "MEDIUM",
                        "Who was the first Christian martyr?",
                        "Stephen",
                        "James,Peter,Barnabas",
                        "Acts 7 records the stoning of Stephen.",
                        "Acts 7"),

                seed("VERSE_COMPLETE", "MEDIUM",
                        "Complete: 'For the wages of sin is death, but the free gift of God is eternal life in ____ Jesus our Lord.'",
                        "Christ",
                        "God,the Spirit,the Father",
                        "Romans 6:23",
                        "Romans 6:23"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which Old Testament book is a collection of love poetry?",
                        "Song of Solomon",
                        "Psalms,Proverbs,Lamentations",
                        "Also called Song of Songs.",
                        "Song of Solomon"),

                seed("VERSE_SPEAKER", "MEDIUM",
                        "Who interpreted Pharaoh's dreams in Egypt?",
                        "Joseph",
                        "Moses,Aaron,Daniel",
                        "Genesis 41 records Joseph's interpretation.",
                        "Genesis 41"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "You're tempted to compromise your integrity at work. Which verse applies?",
                        "'Whatever you do, work heartily, as for the Lord and not for men.'",
                        "'The love of money is the root of all evil.', 'Be still and know that I am God.', 'Ask and it will be given to you.'",
                        "Colossians 3:23 — work as worship.",
                        "Colossians 3:23"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book records the fruit of the Spirit?",
                        "Galatians",
                        "Romans,Ephesians,Colossians",
                        "Galatians 5:22-23.",
                        "Galatians 5:22-23"),

                seed("VERSE_SPEAKER", "MEDIUM",
                        "Who succeeded Moses as leader of Israel?",
                        "Joshua",
                        "Caleb,Aaron,Samuel",
                        "Deuteronomy 31 and the book of Joshua.",
                        "Joshua 1:1-9"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "A believer is considering leaving their church over a minor dispute. What principle applies?",
                        "'Make every effort to keep the unity of the Spirit through the bond of peace.'",
                        "'Judge not, that you be not judged.', 'The Sabbath was made for man.', 'Render to Caesar what is Caesar's.'",
                        "Ephesians 4:3 — unity over preferences.",
                        "Ephesians 4:3"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which epistle says 'faith without works is dead'?",
                        "James",
                        "Romans,Galatians,Hebrews",
                        "James 2:26.",
                        "James 2:26"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which Gospel includes the parable of the Prodigal Son?",
                        "Luke",
                        "Matthew,Mark,John",
                        "Luke 15:11-32.",
                        "Luke 15:11-32"),

                seed("VERSE_SPEAKER", "MEDIUM",
                        "Who wrote the book of Revelation?",
                        "John",
                        "Paul,Peter,James",
                        "Revelation 1:1, 1:9.",
                        "Revelation 1:1"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book describes the valley of dry bones coming to life?",
                        "Ezekiel",
                        "Daniel,Isaiah,Jeremiah",
                        "Ezekiel 37.",
                        "Ezekiel 37"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "A friend is consumed by materialism. Which verse best addresses it?",
                        "'Where your treasure is, there your heart will be also.'",
                        "'Honor your father and mother.', 'Pray without ceasing.', 'Love covers a multitude of sins.'",
                        "Matthew 6:21 — treasure reveals the heart.",
                        "Matthew 6:21"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book has the famous 'love chapter' read at weddings?",
                        "1 Corinthians",
                        "Romans,Ephesians,Colossians",
                        "1 Corinthians 13.",
                        "1 Corinthians 13"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which Old Testament book contains the 'suffering servant' passages?",
                        "Isaiah",
                        "Jeremiah,Ezekiel,Daniel",
                        "Isaiah 52-53.",
                        "Isaiah 52-53"),

                seed("VERSE_SPEAKER", "MEDIUM",
                        "Who was the tax collector that climbed a sycamore tree to see Jesus?",
                        "Zacchaeus",
                        "Matthew,Levi,Nicodemus",
                        "Luke 19:1-10.",
                        "Luke 19:1-10"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "You feel far from God. Which psalm speaks most directly to this?",
                        "'Where can I go from your Spirit? Where can I flee from your presence?'",
                        "'The heavens declare the glory of God.', 'Blessed is the one who does not walk in step with the wicked.', 'The fool says in his heart there is no God.'",
                        "Psalm 139:7 — God's inescapable presence.",
                        "Psalm 139:7"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book records the fiery furnace of Shadrach, Meshach, and Abednego?",
                        "Daniel",
                        "Ezekiel,Jeremiah,Esther",
                        "Daniel 3.",
                        "Daniel 3"),

                seed("VERSE_SPEAKER", "MEDIUM",
                        "Which apostle was known as 'the beloved physician'?",
                        "Luke",
                        "Paul,Timothy,Titus",
                        "Colossians 4:14.",
                        "Colossians 4:14"),

                seed("VERSE_COMPLETE", "MEDIUM",
                        "Complete: 'But they who wait for the LORD shall renew their ____.'",
                        "strength",
                        "joy,hope,courage",
                        "Isaiah 40:31 — the verse quoted in login hero.",
                        "Isaiah 40:31"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "You are praying for a breakthrough and feeling discouraged. Which parable encourages persistence?",
                        "The persistent widow",
                        "The talents,The good Samaritan,The prodigal son",
                        "Luke 18:1-8 — pray and do not lose heart.",
                        "Luke 18:1-8"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book records the Ethiopian eunuch's conversion?",
                        "Acts",
                        "Romans,1 Corinthians,Luke",
                        "Acts 8:26-40.",
                        "Acts 8:26-40"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "A young believer asks how to grow spiritually. Which verse best summarizes?",
                        "'Grow in the grace and knowledge of our Lord and Savior Jesus Christ.'",
                        "'Do not be unequally yoked.', 'Honor your father and mother.', 'Remember the Sabbath day.'",
                        "2 Peter 3:18 — a direct call to growth.",
                        "2 Peter 3:18"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book contains the Aaronic blessing?",
                        "Numbers",
                        "Leviticus,Deuteronomy,Joshua",
                        "Numbers 6:24-26.",
                        "Numbers 6:24-26"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "You're struggling to forgive someone who hurt you deeply. Which verse most convicts?",
                        "'Be kind and compassionate to one another, forgiving each other, just as in Christ God forgave you.'",
                        "'Vengeance is mine, says the Lord.', 'An eye for an eye.', 'Do not cast your pearls before swine.'",
                        "Ephesians 4:32 — forgiveness rooted in Christ's forgiveness.",
                        "Ephesians 4:32"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book records the day of Pentecost?",
                        "Acts",
                        "Luke,John,Romans",
                        "Acts 2.",
                        "Acts 2"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "You're giving into gossip. Which proverb warns you?",
                        "'A gossip separates close friends.'",
                        "'A joyful heart is good medicine.', 'The fear of the Lord is the beginning of wisdom.', 'Train up a child in the way he should go.'",
                        "Proverbs 16:28 — gossip destroys relationships.",
                        "Proverbs 16:28"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book tells of Esther saving her people?",
                        "Esther",
                        "Ruth,Nehemiah,Ezra",
                        "The book of Esther.",
                        "Esther"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "Someone asks how to handle a hostile coworker. What does Romans 12 teach?",
                        "'If it is possible, as far as it depends on you, live at peace with everyone.'",
                        "'Do not associate with unbelievers.', 'Return insult for insult.', 'Only work with believers.'",
                        "Romans 12:18 — pursue peace as far as it depends on you.",
                        "Romans 12:18"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which book records the armour of God?",
                        "Ephesians",
                        "Galatians,Philippians,Colossians",
                        "Ephesians 6:10-18.",
                        "Ephesians 6:10-18"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "You're facing spiritual attack. Which passage prepares you?",
                        "The armour of God",
                        "The Beatitudes,The Lord's Prayer,The Ten Commandments",
                        "Ephesians 6:10-18 describes every piece of spiritual armour.",
                        "Ephesians 6:10-18"),

                seed("VERSE_SPEAKER", "MEDIUM",
                        "Who baptized the Ethiopian eunuch?",
                        "Philip",
                        "Peter,Paul,Apollos",
                        "Acts 8:38.",
                        "Acts 8:26-40"),

                seed("VERSE_SOURCE", "MEDIUM",
                        "Which prophet married Gomer as a symbol of God's love for Israel?",
                        "Hosea",
                        "Amos,Joel,Micah",
                        "Hosea 1-3.",
                        "Hosea 1-3"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "You feel called to give more generously. Which verse encourages you?",
                        "'Each of you should give what you have decided in your heart to give.'",
                        "'Sell everything you have.', 'Give only what is left over.', 'Give to be seen by others.'",
                        "2 Corinthians 9:7 — cheerful, voluntary giving.",
                        "2 Corinthians 9:7"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "Someone asks how to handle a heated argument. Which verse instructs?",
                        "'Everyone should be quick to listen, slow to speak, and slow to become angry.'",
                        "'Speak your mind.', 'Win the argument at any cost.', 'Withdraw and never speak again.'",
                        "James 1:19 — the wisdom of restraint.",
                        "James 1:19"),

                seed("LIFE_APPLICATION", "MEDIUM",
                        "You're trying to be a better spouse or parent. Which verse best guides?",
                        "'Love is patient, love is kind.'",
                        "'Spare the rod and spoil the child.', 'Do not provoke your children.', 'Husbands, love your wives.'",
                        "1 Corinthians 13:4 — love's defining qualities.",
                        "1 Corinthians 13:4"));

        // ============================================================
        // HARD — deeper knowledge, minor prophets, precise references
        // ============================================================
        List<TriviaSeed> hard = List.of(
                seed("VERSE_SOURCE", "HARD",
                        "Which minor prophet wrote the shortest book of the Old Testament?",
                        "Obadiah",
                        "Haggai,Nahum,Habakkuk",
                        "Obadiah has only 21 verses.",
                        "Obadiah"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book is unique for addressing the 'Theodicy' problem — why the righteous suffer?",
                        "Job",
                        "Ecclesiastes,Lamentations,Habakkuk",
                        "The book of Job directly explores this question.",
                        "Job"),

                seed("VERSE_SPEAKER", "HARD",
                        "Which prophet saw a vision of a wheel within a wheel?",
                        "Ezekiel",
                        "Daniel,Zechariah,Isaiah",
                        "Ezekiel 1.",
                        "Ezekiel 1"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book records the confrontation between Elijah and the prophets of Baal on Mount Carmel?",
                        "1 Kings",
                        "2 Kings,1 Samuel,2 Chronicles",
                        "1 Kings 18.",
                        "1 Kings 18"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who was the prophetess who advised King Josiah about the Book of the Law?",
                        "Huldah",
                        "Deborah,Miriam,Anna",
                        "2 Kings 22:14-20.",
                        "2 Kings 22:14-20"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book contains the vision of the four beasts?",
                        "Daniel",
                        "Ezekiel,Zechariah,Revelation",
                        "Daniel 7.",
                        "Daniel 7"),

                seed("VERSE_SOURCE", "HARD",
                        "Which Gospel is the shortest?",
                        "Mark",
                        "Luke,John,Matthew",
                        "Mark has only 16 chapters and is the briefest Gospel.",
                        "Mark"),

                seed("VERSE_SPEAKER", "HARD",
                        "Which minor prophet wrote of the locust plague as a call to repentance?",
                        "Joel",
                        "Amos,Nahum,Hosea",
                        "Joel 1-2.",
                        "Joel 1-2"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who was the governor that rebuilt the wall of Jerusalem?",
                        "Nehemiah",
                        "Ezra,Zerubbabel,Sanballat",
                        "The book of Nehemiah.",
                        "Nehemiah 1-6"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book contains the Melchizedek priestly comparison?",
                        "Hebrews",
                        "Romans,Galatians,1 Peter",
                        "Hebrews 7 develops this theme extensively.",
                        "Hebrews 7"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who was the father of John the Baptist?",
                        "Zacharias",
                        "Simeon,Joseph,Matthan",
                        "Luke 1:5-25.",
                        "Luke 1:5-25"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book commands 'You shall not boil a young goat in its mother's milk'?",
                        "Exodus",
                        "Leviticus,Deuteronomy,Genesis",
                        "Exodus 23:19 (also Deut 14:21).",
                        "Exodus 23:19"),

                seed("VERSE_SOURCE", "HARD",
                        "Which epistle contains the 'faith chapter'?",
                        "Hebrews",
                        "Romans,James,1 Corinthians",
                        "Hebrews 11.",
                        "Hebrews 11"),

                seed("VERSE_SPEAKER", "HARD",
                        "Which prophet ran from God and was swallowed by a great fish?",
                        "Jonah",
                        "Nahum,Amos,Hosea",
                        "The book of Jonah.",
                        "Jonah 1-4"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book of the Bible has the most chapters?",
                        "Psalms",
                        "Isaiah,Jeremiah,Genesis",
                        "Psalms has 150 chapters.",
                        "Psalms"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who replaced Judas among the apostles?",
                        "Matthias",
                        "Barnabas,Silas,Stephen",
                        "Acts 1:26.",
                        "Acts 1:26"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who was the first king of Israel?",
                        "Saul",
                        "David,Solomon,Samuel",
                        "1 Samuel 10.",
                        "1 Samuel 10"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book foretells the new covenant written on hearts?",
                        "Jeremiah",
                        "Ezekiel,Isaiah,Hosea",
                        "Jeremiah 31:31-34.",
                        "Jeremiah 31:31-34"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book describes the throne room vision with seraphim and 'Holy, holy, holy'?",
                        "Isaiah",
                        "Ezekiel,Daniel,Revelation",
                        "Isaiah 6.",
                        "Isaiah 6"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book contains the vision of the valley of dry bones in precise detail?",
                        "Ezekiel",
                        "Daniel,Isaiah,Jeremiah",
                        "Ezekiel 37.",
                        "Ezekiel 37"),

                seed("VERSE_SPEAKER", "HARD",
                        "Which apostle was exiled to the island of Patmos?",
                        "John",
                        "Peter,Paul,James",
                        "Revelation 1:9.",
                        "Revelation 1:9"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book has the earliest prophecy of the coming Messiah as 'the seed of the woman'?",
                        "Genesis",
                        "Isaiah,Jeremiah,Psalms",
                        "Genesis 3:15 — the protoevangelium.",
                        "Genesis 3:15"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book records Paul's speech on Mars Hill?",
                        "Acts",
                        "Romans,1 Corinthians,Galatians",
                        "Acts 17:22-31.",
                        "Acts 17:22-31"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who was the king of Judah who saw the sun go backwards as a sign?",
                        "Hezekiah",
                        "Josiah,Jehoshaphat,Uzziah",
                        "2 Kings 20:8-11.",
                        "2 Kings 20:8-11"),

                seed("VERSE_SPEAKER", "HARD",
                        "Which apostle was called 'Son of Encouragement'?",
                        "Barnabas",
                        "Silas,Titus,Timothy",
                        "Acts 4:36.",
                        "Acts 4:36"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book records the vision of the ram and the goat?",
                        "Daniel",
                        "Ezekiel,Zechariah,Revelation",
                        "Daniel 8.",
                        "Daniel 8"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book contains the parable of the ten virgins?",
                        "Matthew",
                        "Mark,Luke,John",
                        "Matthew 25:1-13.",
                        "Matthew 25:1-13"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who was the first person to see the risen Christ according to John?",
                        "Mary Magdalene",
                        "Peter,John,the other Mary",
                        "John 20:11-18.",
                        "John 20:11-18"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book has the verse 'though he slay me, yet will I hope in him'?",
                        "Job",
                        "Psalms,Lamentations,Habakkuk",
                        "Job 13:15.",
                        "Job 13:15"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book records the man who fled naked when Jesus was arrested?",
                        "Mark",
                        "Matthew,Luke,John",
                        "Mark 14:51-52 — the only Gospel recording this detail.",
                        "Mark 14:51-52"),

                seed("VERSE_SPEAKER", "HARD",
                        "Which prophet confronted David about his sin with Bathsheba?",
                        "Nathan",
                        "Gad,Elijah,Samuel",
                        "2 Samuel 12.",
                        "2 Samuel 12"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book contains the 'song of Mary' (Magnificat)?",
                        "Luke",
                        "Matthew,Mark,John",
                        "Luke 1:46-55.",
                        "Luke 1:46-55"),

                seed("VERSE_SPEAKER", "HARD",
                        "Which prophet was taken up in a whirlwind?",
                        "Elijah",
                        "Enoch,Elisha,Isaiah",
                        "2 Kings 2.",
                        "2 Kings 2"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book presents the seven churches of Revelation in detail?",
                        "Revelation",
                        "Acts,Hebrews,1 Peter",
                        "Revelation 2-3.",
                        "Revelation 2-3"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who was the high priest that questioned Jesus before the Sanhedrin?",
                        "Caiaphas",
                        "Annas,Pilate,Herod",
                        "Matthew 26:57.",
                        "Matthew 26:57"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book has the vision of the four horsemen?",
                        "Revelation",
                        "Daniel,Ezekiel,Zechariah",
                        "Revelation 6.",
                        "Revelation 6"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book contains the 'new heavens and new earth' promise?",
                        "Isaiah",
                        "Revelation,Ezekiel,2 Peter",
                        "Isaiah 65:17 and 66:22 (also 2 Peter 3, Revelation 21).",
                        "Isaiah 65:17"),

                seed("VERSE_SPEAKER", "HARD",
                        "Who succeeded Elijah as prophet?",
                        "Elisha",
                        "Isaiah,Jeremiah,Ezekiel",
                        "1 Kings 19:19-21, 2 Kings 2.",
                        "2 Kings 2"),

                seed("VERSE_SOURCE", "HARD",
                        "Which book has the story of the bones of Elisha reviving a dead man?",
                        "2 Kings",
                        "1 Kings,2 Chronicles,Ezekiel",
                        "2 Kings 13:20-21.",
                        "2 Kings 13:20-21"));

        List<TriviaSeed> all = new java.util.ArrayList<>();
        all.addAll(easy);
        all.addAll(medium);
        all.addAll(hard);

        // Normalize — the seed(...) helper assigns difficulty & type properly
        seeds.saveAll(all);
    }

    /** Build one seed row. */
    private static TriviaSeed seed(
            String type, String difficulty,
            String prompt, String correct, String wrong,
            String explanation, String sourceRef) {
        TriviaSeed s = new TriviaSeed();
        s.setType(type);
        s.setDifficulty(difficulty);
        s.setPrompt(prompt);
        s.setCorrectAnswer(correct);
        s.setWrongAnswers(wrong);
        s.setExplanation(explanation);
        s.setSourceRef(sourceRef);
        s.setActive(true);
        return s;
    }
}