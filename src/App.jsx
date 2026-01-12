import './App.css'

const stats = [
  { label: 'Admissions', value: '48', detail: '+6 today' },
  { label: 'Available Beds', value: '32', detail: 'Capacity 74%' },
  { label: 'Critical Alerts', value: '4', detail: '2 new this hour' },
  { label: 'Staff On Shift', value: '86', detail: 'Night team online' },
]

const patients = [
  {
    name: 'Ava Carter',
    condition: 'Post-op observation',
    room: 'B12',
    doctor: 'Dr. Li',
    status: 'Stabilizing',
  },
  {
    name: 'Miles Green',
    condition: 'Cardiac monitoring',
    room: 'C01',
    doctor: 'Dr. Reyes',
    status: 'Under observation',
  },
  {
    name: 'Sienna Roy',
    condition: 'Neurology consult',
    room: 'A23',
    doctor: 'Dr. Patel',
    status: 'Awaiting scan',
  },
  {
    name: 'Leo Park',
    condition: 'Trauma recovery',
    room: 'D08',
    doctor: 'Dr. Singh',
    status: 'Rehab session',
  },
]

const appointments = [
  {
    time: '08:30 AM',
    patient: 'Noah Ellis',
    doctor: 'Dr. Patel',
    type: 'Cardiology check-in',
  },
  {
    time: '09:45 AM',
    patient: 'Maya Orton',
    doctor: 'Dr. Reyes',
    type: 'Orthopedics review',
  },
  {
    time: '11:00 AM',
    patient: 'Liam Brooks',
    doctor: 'Dr. Singh',
    type: 'Trauma follow-up',
  },
  {
    time: '12:30 PM',
    patient: 'Zoe Chen',
    doctor: 'Dr. Stewart',
    type: 'Telehealth consult',
  },
]

function App() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div>
          <div className="brand">PulseCare</div>
          <p className="brand-subtitle">Hospital Management</p>
        </div>

        <div className="sidebar-divider" />

        <nav className="sidebar-nav">
          <button className="nav-link active">Dashboard</button>
          <button className="nav-link">Patients</button>
          <button className="nav-link">Appointments</button>
          <button className="nav-link">Staff</button>
          <button className="nav-link">Inventory</button>
        </nav>

        <p className="sidebar-note">Monitoring ICU, ER, and telehealth channels in real time.</p>

        <div className="sidebar-footer">
          <span>Need support?</span>
          <button className="ghost">Contact Desk</button>
        </div>
      </aside>

      <main className="main-panel">
        <header className="dash-header">
          <div>
            <p className="muted-label">Hospital Operations</p>
            <h1>PulseCare Control Room</h1>
          </div>
          <div className="header-actions">
            <button className="ghost">Export</button>
            <button className="primary">New Admission</button>
          </div>
        </header>

        <section className="stats-grid">
          {stats.map((stat) => (
            <article key={stat.label} className="stat-card">
              <p className="stat-label">{stat.label}</p>
              <p className="stat-value">{stat.value}</p>
              <p className="stat-detail">{stat.detail}</p>
            </article>
          ))}
        </section>

        <section className="content-grid">
          <article className="card patient-panel">
            <div className="card-header">
              <h2>Active Patients</h2>
              <span className="chip">42 in ICU</span>
            </div>

            <div className="table">
              {patients.map((patient) => (
                <div key={patient.name} className="table-row">
                  <div>
                    <p className="row-name">{patient.name}</p>
                    <p className="row-meta">{patient.condition}</p>
                  </div>
                  <div className="row-meta">
                    <span>{patient.room}</span>
                    <span>{patient.doctor}</span>
                    <span className="status-pill">{patient.status}</span>
                  </div>
                </div>
              ))}
            </div>
          </article>

          <article className="card appointment-panel">
            <div className="card-header">
              <h2>Today's Schedule</h2>
              <span className="chip warning">On time</span>
            </div>

            <ul className="appointment-list">
              {appointments.map((appointment) => (
                <li key={appointment.time}>
                  <div>
                    <p className="row-name">{appointment.time}</p>
                    <p className="row-meta">{appointment.patient}</p>
                  </div>
                  <div>
                    <p className="row-meta">{appointment.doctor}</p>
                    <p className="row-meta">{appointment.type}</p>
                  </div>
                </li>
              ))}
            </ul>
          </article>
        </section>
      </main>
    </div>
  )
}

export default App
