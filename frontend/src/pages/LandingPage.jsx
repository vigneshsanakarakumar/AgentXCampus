import React from 'react';
import { Link } from 'react-router-dom';
import {
  BookOpen,
  LifeBuoy,
  Info,
  Calendar,
  UserCheck,
  Workflow,
  ShieldCheck,
  Eye,
  FileCheck2,
  Lock,
  ArrowRight,
  ChevronRight,
  GraduationCap,
  Briefcase,
  Layers,
  Sparkles
} from 'lucide-react';
import Button from '../components/ui/Button';
import { useAuth } from '../context/AuthContext';

export const LandingPage = () => {
  const { isAuthenticated, role, user } = useAuth();
  const getPortalUrl = () => {
    if (role === 'FACULTY') return '/faculty/dashboard';
    if (role === 'STAFF') return '/staff/dashboard';
    if (role === 'ADMIN') return '/admin/dashboard';
    return '/dashboard';
  };
  return (
    <div className="min-h-screen bg-[var(--color-background)] text-[var(--color-foreground)] flex flex-col">
      {/* Top Navbar */}
      <nav className="sticky top-0 z-50 border-b border-[var(--color-border)] bg-[var(--color-card)]/90 backdrop-blur-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-[var(--color-primary)] flex items-center justify-center text-white font-bold text-sm shadow-sm">
              AX
            </div>
            <span className="font-bold text-lg text-[var(--color-foreground)] tracking-tight">
              AgentX <span className="text-[var(--color-primary)] font-semibold">Campus</span>
            </span>
          </div>

          <div className="hidden md:flex items-center gap-8 text-xs font-medium text-[var(--color-muted-foreground)]">
            <a href="#features" className="hover:text-[var(--color-foreground)] transition-colors">Platform</a>
            <a href="#how-it-works" className="hover:text-[var(--color-foreground)] transition-colors">How it works</a>
            <a href="#roles" className="hover:text-[var(--color-foreground)] transition-colors">Solutions</a>
            <a href="#trust" className="hover:text-[var(--color-foreground)] transition-colors">Security</a>
          </div>

          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <Link to={getPortalUrl()}>
                <Button variant="primary" size="sm" icon={ArrowRight}>
                  Open {role ? role.charAt(0) + role.slice(1).toLowerCase() : ''} Portal
                </Button>
              </Link>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm">
                    Login
                  </Button>
                </Link>
                <Link to="/signup">
                  <Button variant="primary" size="sm">
                    Sign Up
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-20 pb-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto text-center flex-1 flex flex-col items-center justify-center">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-[var(--color-border)] bg-[var(--color-card)] text-xs text-[var(--color-muted-foreground)] mb-6 shadow-sm">
          <span className="w-2 h-2 rounded-full bg-[var(--color-primary)]" />
          <span>Intelligent Campus Operating Layer</span>
        </div>

        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-[var(--color-foreground)] tracking-tight max-w-4xl">
          One Campus. <br className="hidden sm:inline" />
          <span className="text-[var(--color-primary)]">One Intelligent Platform.</span>
        </h1>

        <p className="mt-6 text-base sm:text-lg text-[var(--color-muted-foreground)] max-w-2xl leading-relaxed">
          AgentX Campus connects students, faculty, and campus operations through one intelligent platform for academic services, campus support, events, and everyday workflows.
        </p>

        <div className="mt-8 flex flex-col sm:flex-row items-center gap-3 w-full sm:w-auto">
          {isAuthenticated ? (
            <Link to={getPortalUrl()} className="w-full sm:w-auto">
              <Button size="lg" className="w-full sm:w-auto px-8" icon={ArrowRight}>
                Enter {role ? role.charAt(0) + role.slice(1).toLowerCase() : ''} Dashboard
              </Button>
            </Link>
          ) : (
            <Link to="/login" className="w-full sm:w-auto">
              <Button size="lg" className="w-full sm:w-auto px-8" icon={ArrowRight}>
                Get Started
              </Button>
            </Link>
          )}
          <a href="#features" className="w-full sm:w-auto">
            <Button variant="outline" size="lg" className="w-full sm:w-auto">
              Explore Platform
            </Button>
          </a>
        </div>

        {/* Connected Campus Services Visual Diagram */}
        <div className="mt-14 max-w-3xl w-full p-6 sm:p-8 rounded-2xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-md text-left">
          <div className="flex items-center justify-between pb-4 border-b border-[var(--color-border)]">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-red-400" />
              <div className="w-3 h-3 rounded-full bg-amber-400" />
              <div className="w-3 h-3 rounded-full bg-emerald-400" />
            </div>
            <span className="text-xs text-[var(--color-muted-foreground)] font-mono">agentx-orchestration-mesh</span>
          </div>

          <div className="pt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]">
              <div className="flex items-center gap-2 text-xs font-semibold text-[var(--color-foreground)] mb-1">
                <BookOpen className="w-4 h-4 text-[var(--color-primary)]" />
                <span>Academics</span>
              </div>
              <p className="text-[11px] text-[var(--color-muted-foreground)]">Timetables, CIE records, attendance telemetry & exam schedules</p>
            </div>

            <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]">
              <div className="flex items-center gap-2 text-xs font-semibold text-[var(--color-foreground)] mb-1">
                <LifeBuoy className="w-4 h-4 text-[var(--color-primary)]" />
                <span>Campus Services</span>
              </div>
              <p className="text-[11px] text-[var(--color-muted-foreground)]">Hostel facilities, mess menus, transit GPS & maintenance tickets</p>
            </div>

            <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]">
              <div className="flex items-center gap-2 text-xs font-semibold text-[var(--color-foreground)] mb-1">
                <Workflow className="w-4 h-4 text-[var(--color-primary)]" />
                <span>Student Support</span>
              </div>
              <p className="text-[11px] text-[var(--color-muted-foreground)]">Grievance dispatch, department SLA tracking & event registrations</p>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-[var(--color-card)] border-y border-[var(--color-border)]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-xs uppercase font-bold tracking-widest text-[var(--color-primary)] mb-2">Integrated Infrastructure</h2>
            <h3 className="text-3xl font-bold text-[var(--color-foreground)]">Everything your campus needs, connected.</h3>
            <p className="mt-3 text-sm text-[var(--color-muted-foreground)]">
              A unified operating layer that eliminates fragmented university portals into a single, cohesive experience.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] hover:border-[var(--color-primary)]/40 transition-all">
              <div className="w-10 h-10 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center mb-4">
                <BookOpen className="w-5 h-5" />
              </div>
              <h4 className="font-semibold text-base text-[var(--color-foreground)]">Academic Services</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Timetables, attendance projections, assignments, examinations, and official academic records in real-time.
              </p>
            </div>

            <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] hover:border-[var(--color-primary)]/40 transition-all">
              <div className="w-10 h-10 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center mb-4">
                <LifeBuoy className="w-5 h-5" />
              </div>
              <h4 className="font-semibold text-base text-[var(--color-foreground)]">Campus Support</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Report infrastructure issues, track complaint resolution status, and auto-dispatch tickets to responsible departments.
              </p>
            </div>

            <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] hover:border-[var(--color-primary)]/40 transition-all">
              <div className="w-10 h-10 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center mb-4">
                <Info className="w-5 h-5" />
              </div>
              <h4 className="font-semibold text-base text-[var(--color-foreground)]">Campus Information</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Find institutional policies, faculty directories, hostel guidelines, and transit schedules instantly.
              </p>
            </div>

            <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] hover:border-[var(--color-primary)]/40 transition-all">
              <div className="w-10 h-10 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center mb-4">
                <Calendar className="w-5 h-5" />
              </div>
              <h4 className="font-semibold text-base text-[var(--color-foreground)]">Events & Activities</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Discover workshops, student clubs, hackathons, and campus symposiums with single-click registration.
              </p>
            </div>

            <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] hover:border-[var(--color-primary)]/40 transition-all">
              <div className="w-10 h-10 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center mb-4">
                <UserCheck className="w-5 h-5" />
              </div>
              <h4 className="font-semibold text-base text-[var(--color-foreground)]">Personalized Assistance</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Get context-aware responses tailored specifically to your department, enrolled courses, and campus authorizations.
              </p>
            </div>

            <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] hover:border-[var(--color-primary)]/40 transition-all">
              <div className="w-10 h-10 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center mb-4">
                <Workflow className="w-5 h-5" />
              </div>
              <h4 className="font-semibold text-base text-[var(--color-foreground)]">Intelligent Workflows</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                AgentX coordinates multiple campus services autonomously whenever a task requires cross-department collaboration.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How it Works Section */}
      <section id="how-it-works" className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-xs uppercase font-bold tracking-widest text-[var(--color-primary)] mb-2">Simplicity First</h2>
          <h3 className="text-3xl font-bold text-[var(--color-foreground)]">How it works</h3>
          <p className="mt-3 text-sm text-[var(--color-muted-foreground)]">
            A frictionless interaction paradigm that turns requests into verified campus operations.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] relative">
            <span className="text-3xl font-black text-[var(--color-primary)]/40 font-mono">01</span>
            <h4 className="text-lg font-bold text-[var(--color-foreground)] mt-2">Ask</h4>
            <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
              Describe what you need in natural language—whether asking about a classroom, attendance projection, or reporting a facility defect.
            </p>
          </div>

          <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] relative">
            <span className="text-3xl font-black text-[var(--color-primary)]/40 font-mono">02</span>
            <h4 className="text-lg font-bold text-[var(--color-foreground)] mt-2">AgentX understands</h4>
            <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
              AgentX identifies the request, verifies your role permissions, and connects it with the appropriate specialized campus agent.
            </p>
          </div>

          <div className="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] relative">
            <span className="text-3xl font-black text-[var(--color-primary)]/40 font-mono">03</span>
            <h4 className="text-lg font-bold text-[var(--color-foreground)] mt-2">Get it done</h4>
            <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
              Receive verified information or complete the authorized campus workflow with full activity traceability.
            </p>
          </div>
        </div>
      </section>

      {/* Role-Based Section */}
      <section id="roles" className="py-20 bg-[var(--color-card)] border-y border-[var(--color-border)]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-xs uppercase font-bold tracking-widest text-[var(--color-primary)] mb-2">Tailored Workspaces</h2>
            <h3 className="text-3xl font-bold text-[var(--color-foreground)]">Built for every part of campus.</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]">
              <span className="text-xs font-bold text-[var(--color-primary)] uppercase tracking-wider">Students</span>
              <h4 className="text-base font-semibold text-[var(--color-foreground)] mt-1">Student Portal</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Academic schedules, attendance metrics, event registration, complaints, and campus logistics.
              </p>
            </div>

            <div className="p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]">
              <span className="text-xs font-bold text-[var(--color-primary)] uppercase tracking-wider">Faculty</span>
              <h4 className="text-base font-semibold text-[var(--color-foreground)] mt-1">Faculty Portal</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Course management, student advisee histories, circular drafting, and attendance monitoring.
              </p>
            </div>

            <div className="p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]">
              <span className="text-xs font-bold text-[var(--color-primary)] uppercase tracking-wider">Campus Staff</span>
              <h4 className="text-base font-semibold text-[var(--color-foreground)] mt-1">Staff Portal</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Operational ticket dispatch, department tasks, maintenance schedules, and facility management.
              </p>
            </div>

            <div className="p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]">
              <span className="text-xs font-bold text-[var(--color-primary)] uppercase tracking-wider">Administrators</span>
              <h4 className="text-base font-semibold text-[var(--color-foreground)] mt-1">Admin Portal</h4>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-2 leading-relaxed">
                Campus operations cockpit, Human-in-the-Loop approvals, policy governance, and operational analytics.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Trust & Governance Section */}
      <section id="trust" className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-xs uppercase font-bold tracking-widest text-[var(--color-primary)] mb-2">Institutional Integrity</h2>
          <h3 className="text-3xl font-bold text-[var(--color-foreground)]">Enterprise Trust & Governance</h3>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)]">
            <Lock className="w-5 h-5 text-[var(--color-primary)] mb-3" />
            <h4 className="font-semibold text-sm text-[var(--color-foreground)]">Secure by design</h4>
            <p className="text-xs text-[var(--color-muted-foreground)] mt-1.5 leading-relaxed">
              Role-based access ensures users only see information and actions appropriate to their responsibilities.
            </p>
          </div>

          <div className="p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)]">
            <ShieldCheck className="w-5 h-5 text-[var(--color-primary)] mb-3" />
            <h4 className="font-semibold text-sm text-[var(--color-foreground)]">Human oversight</h4>
            <p className="text-xs text-[var(--color-muted-foreground)] mt-1.5 leading-relaxed">
              Sensitive campus operations mandate authorized human administrative review and approval.
            </p>
          </div>

          <div className="p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)]">
            <FileCheck2 className="w-5 h-5 text-[var(--color-primary)] mb-3" />
            <h4 className="font-semibold text-sm text-[var(--color-foreground)]">Campus knowledge</h4>
            <p className="text-xs text-[var(--color-muted-foreground)] mt-1.5 leading-relaxed">
              Information is grounded strictly in official university handbooks, notices, and institutional databases.
            </p>
          </div>

          <div className="p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)]">
            <Eye className="w-5 h-5 text-[var(--color-primary)] mb-3" />
            <h4 className="font-semibold text-sm text-[var(--color-foreground)]">Activity visibility</h4>
            <p className="text-xs text-[var(--color-muted-foreground)] mt-1.5 leading-relaxed">
              Every automated agent action and tool execution is recorded in immutable audit logs.
            </p>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-[var(--color-border)] bg-[var(--color-card)] py-12 px-4 sm:px-6 lg:px-8 mt-auto">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-3">
            <div className="w-7 h-7 rounded bg-[var(--color-primary)] flex items-center justify-center text-white font-bold text-xs">
              AX
            </div>
            <div>
              <span className="font-bold text-sm text-[var(--color-foreground)]">AgentX Campus</span>
              <p className="text-[11px] text-[var(--color-muted-foreground)]">Intelligent campus operations, connected.</p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-6 text-xs text-[var(--color-muted-foreground)]">
            <a href="#features" className="hover:text-[var(--color-foreground)]">Platform</a>
            <a href="#roles" className="hover:text-[var(--color-foreground)]">Solutions</a>
            <a href="#trust" className="hover:text-[var(--color-foreground)]">Security</a>
            <a href="#how-it-works" className="hover:text-[var(--color-foreground)]">About</a>
            <Link to="/login" className="hover:text-[var(--color-foreground)]">Sign In</Link>
          </div>

          <div className="text-xs text-[var(--color-muted-foreground)]">
            © 2026 AgentX Campus. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
