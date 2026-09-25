import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import Navbar from '../components/ui/Navbar';
import Sidebar from '../components/ui/Sidebar';
import StatCard from '../components/ui/StatCard';
import Card, { CardHeader, CardBody } from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import AgentXAssistant from '../components/ui/AgentXAssistant';
import { AlertCircle, Clock, CheckCircle2, Building2, MapPin } from 'lucide-react';

export const StaffPortal = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStaffData = async () => {
      try {
        const res = await api.get('/staff/dashboard');
        setData(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchStaffData();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-[var(--color-background)]">
        <Navbar />
        <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
          <LoadingSpinner size="lg" message="Loading operations workspace..." />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex flex-col">
      <Navbar />
      <div className="flex-1 flex flex-col md:flex-row">
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />

        <main className="flex-1 p-6 md:p-8 overflow-y-auto max-w-7xl">
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-[var(--color-foreground)] tracking-tight">
              Good morning, {user?.firstName ? (user.firstName + ' ' + user.lastName) : 'Operations Staff'}
            </h1>
            <p className="text-xs text-[var(--color-muted-foreground)] mt-1">
              Campus Facilities & Infrastructure Maintenance Center
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard
              title="Active Tickets"
              value={'' + (data?.activeTickets || 6)}
              subtitle="Grievances awaiting resolution"
              icon={AlertCircle}
              badge={<Badge variant="warning">High Priority</Badge>}
            />
            <StatCard
              title="Open Tickets"
              value={'' + (data?.openTickets || 3)}
              subtitle="Unassigned queue"
              icon={Clock}
              badge={<Badge variant="danger">Pending</Badge>}
            />
            <StatCard
              title="In Progress"
              value={'' + (data?.inProgressTickets || 3)}
              subtitle="Technician dispatched"
              icon={Building2}
              badge={<Badge variant="info">Active</Badge>}
            />
            <StatCard
              title="Resolved (Month)"
              value={'' + (data?.resolvedTickets || 18)}
              subtitle="98% SLA compliance"
              icon={CheckCircle2}
              badge={<Badge variant="success">SLA Met</Badge>}
            />
          </div>

          {activeTab === 'agent' ? (
            <div className="max-w-3xl">
              <AgentXAssistant />
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div className="lg:col-span-2 space-y-6">
                <Card>
                  <CardHeader title="Operational Grievance Queue" subtitle="Real-time campus infrastructure complaints" />
                  <CardBody className="p-0">
                    <div className="divide-y divide-[var(--color-border)]">
                      {(data?.tickets || []).map((t, idx) => (
                        <div key={idx} className="p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-[var(--color-background)]/50 transition-colors">
                          <div>
                            <div className="flex items-center gap-2">
                              <span className="font-mono text-xs font-bold text-[var(--color-primary)]">#{t.ticketNumber}</span>
                              <Badge variant={t.status === 'RESOLVED' ? 'success' : 'warning'} size="sm">{t.status}</Badge>
                              <Badge variant="neutral" size="sm">{t.urgency}</Badge>
                            </div>
                            <p className="text-xs text-[var(--color-foreground)] font-medium mt-1">{t.description}</p>
                            <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5 flex items-center gap-1">
                              <MapPin className="w-3 h-3" />
                              <span>{t.location} • {t.department}</span>
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </CardBody>
                </Card>

                <Card>
                  <CardHeader title="Scheduled Department Tasks" subtitle="Routine maintenance & inspection checklist" />
                  <CardBody className="p-0">
                    <div className="divide-y divide-[var(--color-border)]">
                      {(data?.departmentTasks || []).map((dt, idx) => (
                        <div key={idx} className="p-3.5 flex items-center justify-between hover:bg-[var(--color-background)]/50 transition-colors">
                          <span className="text-xs text-[var(--color-foreground)] font-medium">{dt.task}</span>
                          <Badge variant="neutral" size="sm">{dt.status}</Badge>
                        </div>
                      ))}
                    </div>
                  </CardBody>
                </Card>
              </div>

              <div>
                <AgentXAssistant />
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default StaffPortal;
