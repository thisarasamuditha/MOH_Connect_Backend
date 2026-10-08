#!/usr/bin/env node
/**
 * MOH Connect – demo data seeder.
 *
 * Creates one ADMIN, DOCTOR, MIDWIFE and MOTHER account in a dedicated
 * "Demo PHM Area" and fills every feature with realistic data, using the
 * backend's own REST API (same validation and password hashing as the UI).
 *
 * Usage (backend must be running):
 *   node seed-demo-data.mjs
 *
 * Environment variables:
 *   API_BASE_URL   default http://localhost:8081/api/v1/moh-backend
 *   DEMO_SUFFIX    optional number (1-99999) to create a second, separate demo set
 *   DEMO_PASSWORD  default Demo@2026 (6-30 chars)
 *
 * Writes demo-seed-output.json next to this file; cleanup-demo-data.mjs uses it.
 */
import { writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const BASE = (process.env.API_BASE_URL || "http://localhost:8081/api/v1/moh-backend").replace(/\/$/, "");
const SUFFIX = process.env.DEMO_SUFFIX || "";
const PASSWORD = process.env.DEMO_PASSWORD || "Demo@2026";
const OUTPUT_FILE = join(dirname(fileURLToPath(import.meta.url)), "demo-seed-output.json");

if (SUFFIX && !/^\d{1,5}$/.test(SUFFIX)) {
  console.error("DEMO_SUFFIX must be a number between 1 and 99999");
  process.exit(1);
}
const SUFFIX_NUM = SUFFIX ? Number(SUFFIX) : 0;
const tag = SUFFIX ? `_${SUFFIX}` : "";
const pad5 = String(SUFFIX_NUM).padStart(5, "0");

// ---------------------------------------------------------------- helpers

const today = new Date();
today.setHours(12, 0, 0, 0);

function addDays(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}
function addMonths(date, months) {
  const d = new Date(date);
  d.setMonth(d.getMonth() + months);
  return d;
}
function iso(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}
const daysAgo = (n) => iso(addDays(today, -n));
const daysAhead = (n) => iso(addDays(today, n));

let stepNo = 0;
function step(label) {
  stepNo += 1;
  console.log(`\n[${stepNo}] ${label}`);
}
function ok(msg) {
  console.log(`    ✓ ${msg}`);
}

class ApiError extends Error {
  constructor(method, path, status, body) {
    super(`${method} ${path} -> HTTP ${status}: ${typeof body === "string" ? body : JSON.stringify(body)}`);
    this.status = status;
    this.body = body;
  }
}

async function api(method, path, { token, body } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;
  let res;
  try {
    res = await fetch(`${BASE}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (e) {
    throw new Error(`Cannot reach backend at ${BASE} (${e.cause?.code || e.message}). Is it running?`);
  }
  const text = await res.text();
  let data = text;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    /* plain-text body */
  }
  if (!res.ok) throw new ApiError(method, path, res.status, data);
  return data;
}

const errorText = (e) => (e instanceof ApiError ? JSON.stringify(e.body) : e.message);

async function login(identifier) {
  return api("POST", "/auth/login", { body: { email: identifier, password: PASSWORD } });
}

/** Register a staff account, or log in if it already exists from an earlier run. */
async function registerOrLogin(payload) {
  try {
    await api("POST", "/auth/register", { body: payload });
    ok(`created ${payload.role} '${payload.username}'`);
    return login(payload.username); // login response carries staffId + PHM area
  } catch (e) {
    if (e instanceof ApiError && /already|exists/i.test(errorText(e))) {
      const res = await login(payload.username);
      ok(`${payload.role} '${payload.username}' already existed – logged in`);
      return res;
    }
    throw e;
  }
}

const list = (data) => (Array.isArray(data) ? data : data?.content || data?.data || []);

// ---------------------------------------------------------------- accounts

const accounts = {
  admin: { username: `demo_admin${tag}`, email: `demo.admin${tag}@example.com` },
  doctor: { username: `demo_doctor${tag}`, email: `demo.doctor${tag}@example.com` },
  midwife: { username: `demo_midwife${tag}`, email: `demo.midwife${tag}@example.com` },
  mother: { username: `demo_mother${tag}`, email: `demo.mother${tag}@example.com`, nic: `1995788${pad5}` },
};

const output = {
  createdAt: new Date().toISOString(),
  apiBaseUrl: BASE,
  password: PASSWORD,
  accounts,
  ids: { sessions: [], notifications: [] },
};

function saveOutput() {
  writeFileSync(OUTPUT_FILE, JSON.stringify(output, null, 2));
}

async function main() {
  console.log(`MOH Connect demo seed → ${BASE}`);
  await api("GET", "/phm-areas"); // fail fast if backend is unreachable

  // ------------------------------------------------------------ ADMIN
  step("Admin account");
  const admin = await registerOrLogin({
    username: accounts.admin.username,
    email: accounts.admin.email,
    password: PASSWORD,
    role: "ADMIN",
  });
  output.ids.adminUserId = admin.userId;

  step("Demo PHM area (admin)");
  const areaCode = `DEMO-PHM${tag}`;
  let area = list(await api("GET", "/phm-areas")).find((a) => a.areaCode === areaCode);
  if (!area) {
    area = await api("POST", "/phm-areas", {
      token: admin.token,
      body: { areaName: `Demo PHM Area${SUFFIX ? " " + SUFFIX : ""}`, areaCode },
    });
    ok(`created PHM area ${area.areaName} (id ${area.phmAreaId})`);
  } else {
    ok(`using existing PHM area ${area.areaName} (id ${area.phmAreaId})`);
  }
  output.ids.phmAreaId = area.phmAreaId;

  step("Triposha stock (admin)");
  const batchNumber = `DEMO-TRI${tag}-${iso(today).replace(/-/g, "")}`;
  const stocks = list(await api("GET", "/triposha/stock", { token: admin.token }));
  if (!stocks.some((s) => s.batchNumber === batchNumber)) {
    const stock = await api("POST", "/triposha/stock", {
      token: admin.token,
      body: {
        quantityKg: 60,
        batchNumber,
        receivedDate: daysAgo(90),
        expiryDate: daysAhead(180),
        supplier: "Thriposha Ltd (demo batch)",
      },
    });
    output.ids.triposhaStockId = stock.stockId;
    ok(`added ${stock.quantityKg} kg, batch ${batchNumber}`);
  } else {
    ok(`batch ${batchNumber} already present`);
  }

  // ------------------------------------------------------------ DOCTOR & MIDWIFE
  step("Doctor account");
  const doctor = await registerOrLogin({
    username: accounts.doctor.username,
    email: accounts.doctor.email,
    password: PASSWORD,
    role: "DOCTOR",
    doctorDetails: {
      name: "Dr. Nadeesha Jayawardena",
      specialization: "Medical Officer of Health – Maternal & Child Health",
      phmAreaId: area.phmAreaId,
      contactNumber: "0771234501",
      email: accounts.doctor.email,
      licenseNumber: `SLMC-DEMO-${pad5}`,
    },
  });
  output.ids.doctorUserId = doctor.userId;
  output.ids.doctorId = doctor.staffId;

  step("Midwife account");
  const midwife = await registerOrLogin({
    username: accounts.midwife.username,
    email: accounts.midwife.email,
    password: PASSWORD,
    role: "MIDWIFE",
    midwifeDetails: {
      name: "Kumari Herath",
      phmAreaId: area.phmAreaId,
      contactNumber: "0771234502",
      email: accounts.midwife.email,
      assignmentDate: daysAgo(400),
      qualifications: "Diploma in Midwifery (NIHS Kalutara)",
    },
  });
  output.ids.midwifeUserId = midwife.userId;
  output.ids.midwifeId = midwife.staffId;
  if (!midwife.staffId) throw new Error("Midwife login did not return a staffId (midwife profile missing)");
  saveOutput();

  // ------------------------------------------------------------ MOTHER
  step("Mother registration (midwife → New Registration)");
  let mother;
  let resumed = false;
  try {
    mother = await api("POST", "/mothers/register", {
      token: midwife.token,
      body: {
        username: accounts.mother.username,
        email: accounts.mother.email,
        password: PASSWORD,
        nic: accounts.mother.nic,
        name: "Sanduni Wijesinghe",
        address: "No. 24, Temple Road, Peradeniya",
        dateOfBirth: "1995-06-14",
        occupation: "Teacher",
        contactNumber: "0771234503",
        bloodGroup: "O+",
        registrationDate: daysAgo(140),
        allergies: "Penicillin",
        husbandName: "Kasun Wijesinghe",
        husbandNic: `1992455${pad5}`,
        husbandDob: "1992-02-20",
        husbandPhone: "0771234504",
        husbandEmail: `kasun.demo${tag}@example.com`,
      },
    });
  } catch (e) {
    if (!(e instanceof ApiError && /already/i.test(errorText(e)))) throw e;
    // An earlier run stopped part-way (e.g. before DB/07 was applied): resume it.
    const me = await api("GET", "/mothers/me", { token: (await login(accounts.mother.username)).token });
    if (me.activePregnancy) {
      throw new Error(
        "The demo mother already has a pregnancy – demo data was fully seeded before.\n" +
          "Run cleanup-demo-data.mjs first, or set DEMO_SUFFIX=<number> to create a separate demo set.",
      );
    }
    mother = { motherId: me.mother.motherId, username: accounts.mother.username };
    resumed = true;
  }
  const motherId = mother.motherId;
  output.ids.motherId = motherId;
  saveOutput();
  ok(`mother '${mother.username}' ${resumed ? "already registered – resuming" : "registered"} (motherId ${motherId})`);

  const schedules = list(await api("GET", "/vaccine-schedules"));

  // ------------------------------------------------------------ CHILD (older sibling)
  step("Older child – 14 months (midwife → Child Care)");
  const babyDob = addMonths(today, -14);
  const existingBaby = resumed
    ? list(await api("GET", `/babies?motherId=${motherId}`, { token: midwife.token }))[0]
    : null;
  const overdue = [];
  if (existingBaby) {
    output.ids.babyId = existingBaby.babyId;
    ok(`child already created in an earlier run (babyId ${existingBaby.babyId}) – skipping child steps`);
  } else {
  const baby = await api("POST", "/babies", {
    token: midwife.token,
    body: {
      motherId,
      name: "Nethmi Wijesinghe",
      dateOfBirth: iso(babyDob),
      gender: "FEMALE",
      birthWeight: 3.1,
      birthHeight: 49.5,
      apgarScore: "9/10",
      birthOrder: 1,
      hospitalBorn: "Teaching Hospital Peradeniya",
      birthComplications: "None – elective caesarean section",
      specialNotes: "Demo child record",
    },
  });
  const babyId = baby.babyId;
  output.ids.babyId = babyId;
  ok(`baby '${baby.name}' (babyId ${babyId}), born ${iso(babyDob)}`);

  step("Growth monitoring visits");
  const growth = [
    { m: 1, w: 4.2, h: 53.5, hc: 36.5, feed: "Exclusive Breastfeeding", dev: "Lifts head briefly, responds to sound" },
    { m: 2, w: 5.1, h: 57.0, hc: 38.3, feed: "Exclusive Breastfeeding", dev: "Social smile, follows objects" },
    { m: 4, w: 6.4, h: 62.0, hc: 40.6, feed: "Exclusive Breastfeeding", dev: "Holds head steady, laughs, reaches for toys" },
    { m: 6, w: 7.3, h: 65.8, hc: 42.2, feed: "Complementary feeding started + breastfeeding", dev: "Sits with support, rolls over" },
    { m: 9, w: 8.2, h: 70.2, hc: 43.8, feed: "Complementary feeding + breastfeeding", dev: "Sits without support, crawls, babbles" },
    { m: 12, w: 8.9, h: 74.0, hc: 44.9, feed: "Family diet + breastfeeding", dev: "Stands with support, says 1–2 words" },
    { m: 13.5, w: 9.3, h: 76.2, hc: 45.4, feed: "Family diet + breastfeeding", dev: "Walks a few steps alone, waves bye-bye" },
  ];
  for (let i = 0; i < growth.length; i++) {
    const g = growth[i];
    const visitDate = addDays(babyDob, Math.round(g.m * 30.4));
    const next = i + 1 < growth.length ? addDays(babyDob, Math.round(growth[i + 1].m * 30.4)) : addDays(today, 18);
    const bmi = +(g.w / (g.h / 100) ** 2).toFixed(1);
    await api("POST", `/baby-records?babyId=${babyId}&midwifeId=${midwife.staffId}`, {
      token: midwife.token,
      body: {
        recordDate: iso(visitDate),
        ageMonths: Math.floor(g.m),
        weight: g.w,
        height: g.h,
        headCircumference: g.hc,
        bmi,
        developmentalMilestones: g.dev,
        breastfeedingStatus: g.feed,
        healthStatus: "Healthy",
        temperature: "36.8",
        skinColor: "Normal",
        eyeColor: "Normal",
        umbilicalCordStatus: g.m <= 1 ? "Healed" : null,
        findings: "Growth on track",
        recommendations: g.m < 6 ? "Continue exclusive breastfeeding" : "Continue breastfeeding with age-appropriate complementary foods",
        nextVisitDate: iso(next),
      },
    });
  }
  ok(`${growth.length} growth visits (1 → 13.5 months)`);

  step("Child immunizations (Sri Lanka EPI schedule from DB)");
  const babyAgeDays = Math.round((today - babyDob) / 86400000);
  const babySchedules = schedules
    .filter((s) => s.targetGroup === "BABY" && s.isActive !== false && !s.isConditional)
    .filter((s) => !s.targetGender || s.targetGender === "FEMALE")
    .filter((s) => s.recommendedAgeDays != null && s.recommendedAgeDays <= babyAgeDays - 7)
    .sort((a, b) => a.recommendedAgeDays - b.recommendedAgeDays);
  // Leave the 12-month dose(s) unrecorded so the "Delayed" status and the
  // due/overdue lists have something to show.
  const leaveOverdue = (s) => s.recommendedAgeDays >= 330 && s.recommendedAgeDays <= 400;
  const given = [];
  for (const s of babySchedules) {
    if (leaveOverdue(s)) {
      overdue.push(`${s.vaccineName} dose ${s.doseNumber}`);
      continue;
    }
    const date = addDays(babyDob, s.recommendedAgeDays + 3);
    try {
      await api("POST", "/baby-vaccinations", {
        token: midwife.token,
        body: {
          babyId,
          midwifeId: midwife.staffId,
          scheduleId: s.scheduleId,
          vaccinationDate: iso(date),
          batchNumber: `DEMO-${s.vaccineName.replace(/[^A-Za-z0-9]/g, "").toUpperCase()}-${s.doseNumber}`,
          manufacturer: "Demo Pharma",
        },
      });
      given.push(`${s.vaccineName} ${s.doseNumber}`);
    } catch (e) {
      console.log(`    ! skipped ${s.vaccineName} dose ${s.doseNumber}: ${errorText(e)}`);
    }
  }
  ok(`${given.length} doses recorded: ${given.join(", ") || "none"}`);
  if (overdue.length) ok(`left overdue on purpose: ${overdue.join(", ")}`);
  if (!babySchedules.length) console.log("    ! no active BABY vaccine schedules found – has DB/07 been run?");

  step("Child Thriposha");
  for (const d of [60, 30]) {
    await api("POST", "/triposha/baby", {
      token: midwife.token,
      body: { babyId, midwifeId: midwife.staffId, quantityKg: 1.5, distributionDate: daysAgo(d), remarks: "Monthly issue – collected by mother" },
    });
  }
  ok("2 issues of 1.5 kg");
  } // end of child steps

  // ------------------------------------------------------------ CURRENT PREGNANCY
  step("Current pregnancy – 28 weeks (midwife → Maternal Care)");
  const lmp = addDays(today, -28 * 7);
  const pregnancy = await api("POST", `/pregnancies?motherId=${motherId}`, {
    token: midwife.token,
    body: {
      lmpDate: iso(lmp),
      eddDate: iso(addDays(lmp, 280)),
      gravida: 2,
      para: 1,
      riskLevel: "MEDIUM",
      riskFactors: "Previous caesarean section; mild anaemia in first trimester",
    },
  });
  const pregnancyId = pregnancy.pregnancyId;
  output.ids.pregnancyId = pregnancyId;
  ok(`pregnancy ${pregnancy.pregnancyNumber || pregnancyId}, LMP ${iso(lmp)}, EDD ${iso(addDays(lmp, 280))}`);

  step("Antenatal (ANC) visits");
  const anc = [
    { wk: 8, visitType: "BOOKING", weight: 54.0, bmi: 22.5, bp: "110/70", shf: null, hb: 10.4,
      findings: "Booking visit. Mild anaemia (Hb 10.4). Dating scan consistent with LMP.",
      rec: "Iron, folic acid and vitamin C daily. Dietary advice given. Repeat Hb at 16 weeks." },
    { wk: 16, visitType: "ROUTINE", weight: 56.2, bmi: 23.4, bp: "112/72", shf: 16, hb: 11.0,
      findings: "Feeling well. Fetal heart heard. Hb improving.", rec: "Continue supplements. Anomaly scan at 20–22 weeks." },
    { wk: 22, visitType: "ROUTINE", weight: 58.4, bmi: 24.3, bp: "118/76", shf: 22, hb: 11.4,
      findings: "Anomaly scan normal. Fetal movements felt.", rec: "Continue supplements. OGTT at 24–28 weeks." },
    { wk: 27, visitType: "ROUTINE", weight: 60.1, bmi: 25.0, bp: "126/84", shf: 27, hb: 11.6,
      findings: "Mild ankle oedema. BP slightly higher than before. OGTT normal.",
      rec: "Monitor BP at next visit, rest with legs raised, report headache or visual changes." },
  ];
  const recordIds = [];
  for (let i = 0; i < anc.length; i++) {
    const v = anc[i];
    const visitDate = addDays(lmp, v.wk * 7);
    const next = i + 1 < anc.length ? addDays(lmp, anc[i + 1].wk * 7) : addDays(today, 10);
    const rec = await api("POST", `/mother-records?pregnancyId=${pregnancyId}&midwifeId=${midwife.staffId}`, {
      token: midwife.token,
      body: {
        recordDate: iso(visitDate),
        visitType: v.visitType,
        gestationalAge: v.wk,
        weight: v.weight,
        bmi: v.bmi,
        bloodPressure: v.bp,
        shf: v.shf,
        hemoglobin: v.hb,
        findings: v.findings,
        recommendations: v.rec,
        complications: i === 0 ? "Mild anaemia" : null,
        nextVisitDate: iso(next),
      },
    });
    recordIds.push(rec.recordId);
  }
  output.ids.motherRecordIds = recordIds;
  ok(`${anc.length} ANC visits (8, 16, 22, 27 weeks)`);

  step("Doctor reviews ANC records (doctor → Review queue)");
  for (const [i, id] of recordIds.slice(0, 3).entries()) {
    const comment = i === 0 ? "Anaemia noted – agree with iron therapy." : "Reviewed – normal progress.";
    await api("PUT", `/mother-records/${id}/review?status=APPROVED&comment=${encodeURIComponent(comment)}`, { token: doctor.token });
  }
  ok("3 visits APPROVED, latest (27 weeks) left SUBMITTED in the doctor's review queue");

  step("Maternal vaccinations");
  const motherSchedules = schedules
    .filter((s) => s.targetGroup === "MOTHER" && s.isActive !== false)
    .sort((a, b) => (a.doseNumber || 0) - (b.doseNumber || 0) || a.scheduleId - b.scheduleId)
    .slice(0, 2);
  for (const [i, s] of motherSchedules.entries()) {
    await api("POST", "/mother-vaccinations", {
      token: midwife.token,
      body: {
        pregnancyId,
        midwifeId: midwife.staffId,
        scheduleId: s.scheduleId,
        vaccinationDate: iso(addDays(lmp, (i === 0 ? 16 : 22) * 7)),
        batchNumber: `DEMO-${s.vaccineName.replace(/[^A-Za-z0-9]/g, "").toUpperCase()}-M${s.doseNumber || i + 1}`,
        manufacturer: "Demo Pharma",
      },
    });
  }
  ok(motherSchedules.length ? `recorded ${motherSchedules.map((s) => `${s.vaccineName} ${s.doseNumber}`).join(", ")}` : "no MOTHER schedules found – skipped");

  step("Maternal Thriposha");
  for (const wk of [16, 22]) {
    await api("POST", "/triposha/mother", {
      token: midwife.token,
      body: { pregnancyId, midwifeId: midwife.staffId, quantityKg: 1.5, distributionDate: iso(addDays(lmp, wk * 7)), remarks: "Collected by mother" },
    });
  }
  ok("2 issues of 1.5 kg");

  // ------------------------------------------------------------ CLINICS
  step("Clinic sessions and attendance (midwife → Clinics & Sessions)");
  const sessionTypes = list(await api("GET", "/session-types"));
  if (!sessionTypes.length) throw new Error("No session types exist – ask an admin to create one first");
  const pickType = (re) => sessionTypes.find((t) => re.test(t.typeName || "")) || sessionTypes[0];
  const ancType = pickType(/ante|anc|matern|pregnan|clinic/i);
  const childType = pickType(/child|well|immun|baby|infant/i);

  const sessionPlans = [
    { type: ancType, date: daysAgo(21), topic: "Antenatal clinic – third trimester check", completed: true, attended: true },
    { type: ancType, date: daysAhead(10), topic: "Antenatal clinic – BP & growth review" },
    { type: childType, date: daysAhead(20), topic: "Child welfare clinic – 18-month vaccines & growth" },
  ];
  for (const p of sessionPlans) {
    const base = {
      sessionDate: p.date,
      startTime: "08:30:00",
      endTime: "12:00:00",
      topic: p.topic,
      venue: "Demo PHM Area Clinic Centre",
      description: "Demo session",
      capacity: 30,
    };
    const session = await api("POST", "/sessions", {
      token: midwife.token,
      body: { ...base, midwifeId: midwife.staffId, sessionTypeId: p.type.sessionTypeId, phmAreaId: area.phmAreaId },
    });
    output.ids.sessions.push(session.sessionId);
    const attendance = await api("POST", "/session-attendance", {
      token: midwife.token,
      body: { sessionId: session.sessionId, motherId, notes: "Registered by midwife" },
    });
    if (p.attended) {
      await api("PUT", `/session-attendance/${attendance.attendanceId}/mark`, {
        token: midwife.token,
        body: { attended: true, feedback: "Attended – BP 124/80, advised rest" },
      });
    }
    if (p.completed) {
      await api("PUT", `/sessions/${session.sessionId}`, { token: midwife.token, body: { ...base, status: "COMPLETED" } });
    }
    ok(`${p.date}  ${p.topic}${p.completed ? " (completed, attended)" : " (mother registered)"}`);
  }
  saveOutput();

  // ------------------------------------------------------------ NOTIFICATIONS
  step("Notifications to the mother");
  let types = list(await api("GET", "/notification-types", { token: midwife.token }));
  if (!types.length) {
    types = [
      await api("POST", "/notification-types", {
        token: admin.token,
        body: { typeName: "Clinic Reminder", description: "Upcoming clinic reminder", template: "Reminder: {message}", priority: "MEDIUM" },
      }),
    ];
  }
  const pickNotif = (re) => types.find((t) => re.test(t.typeName || "")) || types[0];
  const notifications = [
    { type: pickNotif(/clinic|appoint|remind/i), msg: `Your next antenatal clinic is on ${daysAhead(10)} at 8.30 am, Demo PHM Area Clinic Centre.`, eventDate: daysAhead(10), eventType: "CLINIC" },
    { type: pickNotif(/vaccin|immun/i), msg: `Nethmi's 12-month vaccine${overdue.length ? ` (${overdue.join(", ")})` : ""} is overdue. Please bring her to the next child clinic.`, eventDate: daysAhead(20), eventType: "VACCINATION" },
    { type: pickNotif(/visit|home|anc|remind/i), msg: "Please check your blood pressure if you get headaches or blurred vision, and contact your midwife.", eventDate: daysAgo(3), eventType: "ANC" },
  ];
  for (const n of notifications) {
    const created = await api("POST", "/notifications", {
      token: midwife.token,
      body: { typeId: n.type.typeId, midwifeId: midwife.staffId, motherId, message: n.msg, deliveryMethod: "SMS", eventDate: n.eventDate, eventType: n.eventType },
    });
    output.ids.notifications.push(created.notificationId);
  }
  ok(`${notifications.length} notifications`);

  step("Mother logs in and reads one notification");
  const motherLogin = await login(accounts.mother.username);
  output.ids.motherUserId = motherLogin.userId;
  await api("PUT", `/notifications/${output.ids.notifications[2]}/read`, { token: motherLogin.token });
  ok("1 read, 2 unread");

  saveOutput();
  printCredentials();
}

function printCredentials() {
  console.log("\n================ DEMO LOGINS ================");
  console.log(`Password for all four accounts: ${PASSWORD}\n`);
  for (const [role, a] of Object.entries(accounts)) {
    console.log(`${role.toUpperCase().padEnd(8)} ${a.username.padEnd(20)} ${a.email}${a.nic ? `   (NIC ${a.nic})` : ""}`);
  }
  console.log(`\nIDs saved to ${OUTPUT_FILE}`);
}

main().catch((e) => {
  saveOutput();
  console.error(`\n✗ Seed failed at step ${stepNo}: ${e.message}`);
  if (/Unknown column/i.test(e.message)) {
    console.error("→ The live DB is missing columns: run DB/07_clinical_workflows.sql, then run this script again (it resumes).");
  }
  console.error(`Partial IDs saved to ${OUTPUT_FILE} (cleanup-demo-data.mjs can remove them).`);
  process.exit(1);
});
